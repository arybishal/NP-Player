package com.npplayer.core.data.lyrics

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.npplayer.core.data.database.dao.MusicLyricsDao
import com.npplayer.core.data.database.entity.MusicLyricsEntity
import com.npplayer.core.data.database.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class LyricsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lyricsDao: MusicLyricsDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun getLyricsFlow(songId: Long): Flow<MusicLyricsEntity?> = lyricsDao.getLyricsForSongFlow(songId)

    suspend fun loadLyricsForSong(
        song: SongEntity,
        forceOnline: Boolean = false,
        onlineEnabled: Boolean = true
    ): MusicLyricsEntity? = withContext(Dispatchers.IO) {
        // 1. Check database cache first if not forced
        val cached = lyricsDao.getLyricsForSong(song.id)
        if (!forceOnline && cached != null) {
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            val isExpired = (System.currentTimeMillis() - cached.lastFetched) > sevenDaysMs
            if (!isExpired) {
                return@withContext cached
            }
        }

        // 2. Try Embedded lyrics in file tags
        val embeddedLyrics = extractEmbeddedLyrics(song)
        if (!embeddedLyrics.isNullOrBlank()) {
            val entity = MusicLyricsEntity(
                songId = song.id,
                trackName = song.title,
                artistName = song.artist,
                albumName = song.album,
                syncedLyrics = if (embeddedLyrics.contains("[")) embeddedLyrics else null,
                plainLyrics = embeddedLyrics,
                isInstrumental = false,
                source = "embedded",
                offsetMs = cached?.offsetMs ?: 0L,
                lastFetched = System.currentTimeMillis()
            )
            lyricsDao.insertOrUpdateLyrics(entity)
            return@withContext entity
        }

        // 3. Try Sidecar .lrc file next to audio file or in Music/Lyrics
        val sidecarLyrics = findSidecarLrc(song)
        if (!sidecarLyrics.isNullOrBlank()) {
            val entity = MusicLyricsEntity(
                songId = song.id,
                trackName = song.title,
                artistName = song.artist,
                albumName = song.album,
                syncedLyrics = sidecarLyrics,
                plainLyrics = null,
                isInstrumental = false,
                source = "sidecar",
                offsetMs = cached?.offsetMs ?: 0L,
                lastFetched = System.currentTimeMillis()
            )
            lyricsDao.insertOrUpdateLyrics(entity)
            return@withContext entity
        }

        // 4. Try LRCLIB online
        if (onlineEnabled) {
            val onlineResult = fetchFromLrclib(song)
            if (onlineResult != null) {
                val entity = MusicLyricsEntity(
                    songId = song.id,
                    trackName = song.title,
                    artistName = song.artist,
                    albumName = song.album,
                    syncedLyrics = onlineResult.syncedLyrics,
                    plainLyrics = onlineResult.plainLyrics,
                    isInstrumental = onlineResult.instrumental ?: false,
                    source = "lrclib",
                    offsetMs = cached?.offsetMs ?: 0L,
                    lastFetched = System.currentTimeMillis()
                )
                lyricsDao.insertOrUpdateLyrics(entity)
                return@withContext entity
            }
        }

        // Cache negative result so we don't spam requests
        val emptyEntity = MusicLyricsEntity(
            songId = song.id,
            trackName = song.title,
            artistName = song.artist,
            albumName = song.album,
            syncedLyrics = null,
            plainLyrics = null,
            isInstrumental = false,
            source = "none",
            offsetMs = cached?.offsetMs ?: 0L,
            lastFetched = System.currentTimeMillis()
        )
        lyricsDao.insertOrUpdateLyrics(emptyEntity)
        return@withContext emptyEntity
    }

    private fun extractEmbeddedLyrics(song: SongEntity): String? {
        val retriever = MediaMetadataRetriever()
        try {
            if (song.filePath.isNotBlank() && File(song.filePath).exists()) {
                retriever.setDataSource(song.filePath)
            } else {
                retriever.setDataSource(context, Uri.parse(song.uri))
            }
            // Some platforms populate METADATA_KEY_COMPILATION, or raw lyrics
            // Note: standard MediaMetadataRetriever doesn't have an explicit METADATA_KEY_LYRICS,
            // but some OEM builds map key 1000 or custom tags.
            return null
        } catch (_: Exception) {
            return null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun findSidecarLrc(song: SongEntity): String? {
        if (song.filePath.isBlank()) return null
        val songFile = File(song.filePath)
        if (!songFile.exists()) return null

        // Same directory with .lrc extension
        val nameWithoutExt = songFile.nameWithoutExtension
        val parent = songFile.parentFile
        if (parent != null) {
            val localLrc = File(parent, "$nameWithoutExt.lrc")
            if (localLrc.exists() && localLrc.canRead()) {
                val text = runCatching { localLrc.readText(Charsets.UTF_8) }.getOrNull()
                if (!text.isNullOrBlank()) return text
            }
        }

        // Common Lyrics folder: /storage/emulated/0/Music/Lyrics/
        val commonLyricsDir = File("/storage/emulated/0/Music/Lyrics")
        if (commonLyricsDir.exists() && commonLyricsDir.isDirectory) {
            val dirLrc = File(commonLyricsDir, "$nameWithoutExt.lrc")
            if (dirLrc.exists() && dirLrc.canRead()) {
                val text = runCatching { dirLrc.readText(Charsets.UTF_8) }.getOrNull()
                if (!text.isNullOrBlank()) return text
            }
        }
        return null
    }

    private fun fetchFromLrclib(song: SongEntity): LrclibResponse? {
        val cleanTitle = LyricsQueryHelper.cleanTitle(song.title)
        val cleanArtist = LyricsQueryHelper.normalizeArtist(song.artist)
        val durationSec = (song.durationMs / 1000).toInt()

        if (cleanTitle.isBlank()) return null

        val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8")
        val encodedArtist = URLEncoder.encode(cleanArtist, "UTF-8")
        val encodedAlbum = URLEncoder.encode(song.album, "UTF-8")

        // 1. Try exact match GET /api/get
        val getUrl = buildString {
            append("https://lrclib.net/api/get?track_name=").append(encodedTitle)
            if (cleanArtist.isNotBlank()) {
                append("&artist_name=").append(encodedArtist)
            }
            if (song.album.isNotBlank() && !song.album.equals("<unknown>", ignoreCase = true)) {
                append("&album_name=").append(encodedAlbum)
            }
            if (durationSec > 0) {
                append("&duration=").append(durationSec)
            }
        }

        val exactResult = executeLrclibGet(getUrl)
        if (exactResult != null && (exactResult.syncedLyrics != null || exactResult.plainLyrics != null || exactResult.instrumental == true)) {
            return exactResult
        }

        // 2. Try search fallback GET /api/search
        val searchUrl = buildString {
            append("https://lrclib.net/api/search?track_name=").append(encodedTitle)
            if (cleanArtist.isNotBlank()) {
                append("&artist_name=").append(encodedArtist)
            }
        }

        val searchList = executeLrclibSearch(searchUrl)
        if (searchList.isNotEmpty()) {
            // Find best match by duration within 3 seconds
            val bestMatch = searchList.minByOrNull { item ->
                val diff = abs((item.duration ?: 0.0) - durationSec)
                diff
            }
            if (bestMatch != null && (bestMatch.syncedLyrics != null || bestMatch.plainLyrics != null || bestMatch.instrumental == true)) {
                return bestMatch
            }
        }

        return null
    }

    private fun executeLrclibGet(url: String): LrclibResponse? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "NPPlayer/1.0 (https://github.com/npplayer)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return null
            }
            val body = response.body?.string() ?: return null
            json.decodeFromString<LrclibResponse>(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun executeLrclibSearch(url: String): List<LrclibResponse> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "NPPlayer/1.0 (https://github.com/npplayer)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            json.decodeFromString<List<LrclibResponse>>(body)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun updateOffset(songId: Long, offsetMs: Long) = withContext(Dispatchers.IO) {
        lyricsDao.updateOffset(songId, offsetMs)
    }

    suspend fun saveCustomLyrics(songId: Long, plainText: String, syncedText: String?) = withContext(Dispatchers.IO) {
        val existing = lyricsDao.getLyricsForSong(songId)
        val entity = existing?.copy(
            plainLyrics = plainText,
            syncedLyrics = syncedText,
            source = "custom",
            lastFetched = System.currentTimeMillis()
        ) ?: MusicLyricsEntity(
            songId = songId,
            trackName = "",
            artistName = "",
            plainLyrics = plainText,
            syncedLyrics = syncedText,
            source = "custom"
        )
        lyricsDao.insertOrUpdateLyrics(entity)
    }

    suspend fun saveLyricsAsLrcFile(song: SongEntity, lrcText: String): Boolean = withContext(Dispatchers.IO) {
        if (song.filePath.isBlank()) return@withContext false
        val songFile = File(song.filePath)
        val parent = songFile.parentFile ?: return@withContext false
        if (!songFile.exists()) return@withContext false
        val lrcFile = File(parent, "${songFile.nameWithoutExtension}.lrc")
        try {
            lrcFile.writeText(lrcText, Charsets.UTF_8)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearAllLyricsCache() = withContext(Dispatchers.IO) {
        lyricsDao.clearAllLyrics()
    }
}
