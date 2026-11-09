package com.npplayer.core.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.npplayer.core.data.database.dao.SongDao
import com.npplayer.core.data.database.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioMediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao
) {
    private val scannerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var contentObserver: ContentObserver? = null

    // Default excluded folders to filter voice notes and call recordings
    private val defaultExcludedFolders = listOf(
        "call", "recordings", "recording", "voice notes", "voicenotes",
        "whatsapp audio/sent", "telegram audio", ".thumbnails", "sounds/ringtones"
    )

    init {
        registerObserver()
    }

    private fun registerObserver() {
        try {
            contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    scannerScope.launch {
                        scanAudio()
                    }
                }
            }
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver!!
            )
        } catch (_: Exception) {
            // ContentObserver registration might fail if permission not yet granted
        }
    }

    suspend fun scanAudio(
        minDurationMs: Long = 30_000L,
        excludedFolders: List<String> = defaultExcludedFolders
    ): AudioScanResult = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val resolver: ContentResolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = buildList {
                add(MediaStore.Audio.Media._ID)
                add(MediaStore.Audio.Media.TITLE)
                add(MediaStore.Audio.Media.ARTIST)
                add(MediaStore.Audio.Media.ALBUM)
                add(MediaStore.Audio.Media.ALBUM_ID)
                add(MediaStore.Audio.Media.DURATION)
                add(MediaStore.Audio.Media.SIZE)
                add(MediaStore.Audio.Media.DATA)
                add(MediaStore.Audio.Media.DATE_ADDED)
                add(MediaStore.Audio.Media.DATE_MODIFIED)
                add(MediaStore.Audio.Media.MIME_TYPE)
                add(MediaStore.Audio.Media.YEAR)
                add(MediaStore.Audio.Media.TRACK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(MediaStore.Audio.Media.ALBUM_ARTIST)
                    add(MediaStore.Audio.Media.BITRATE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(MediaStore.Audio.Media.GENRE)
                }
            }.toTypedArray()

            val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(minDurationMs.toString())

            val scannedSongs = mutableListOf<SongEntity>()
            val foundMediaStoreIds = mutableSetOf<Long>()

            resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val mimeTypeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val albumArtistCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
                } else -1
                val bitrateCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
                } else -1
                val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1

                while (cursor.moveToNext()) {
                    val mediaStoreId = cursor.getLong(idCol)
                    foundMediaStoreIds.add(mediaStoreId)

                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    if (path.isBlank()) continue

                    // Check excluded folders
                    val lowerPath = path.lowercase().replace('\\', '/')
                    if (excludedFolders.any { excluded -> lowerPath.contains(excluded.lowercase()) }) {
                        continue
                    }

                    val title = if (titleCol >= 0) cursor.getString(titleCol) ?: "Unknown" else "Unknown"
                    val artist = if (artistCol >= 0) {
                        val a = cursor.getString(artistCol)
                        if (a.isNullOrBlank() || a == "<unknown>") "Unknown Artist" else a
                    } else "Unknown Artist"

                    val album = if (albumCol >= 0) {
                        val al = cursor.getString(albumCol)
                        if (al.isNullOrBlank() || al == "<unknown>") "Unknown Album" else al
                    } else "Unknown Album"

                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val dateModified = if (dateModifiedCol >= 0) cursor.getLong(dateModifiedCol) else 0L
                    val mimeType = if (mimeTypeCol >= 0) cursor.getString(mimeTypeCol) ?: "audio/*" else "audio/*"
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                    val trackRaw = if (trackCol >= 0) cursor.getInt(trackCol) else 0
                    val trackNo = if (trackRaw >= 1000) trackRaw % 1000 else trackRaw
                    val discNo = if (trackRaw >= 1000) trackRaw / 1000 else 1

                    val albumArtist = if (albumArtistCol >= 0) cursor.getString(albumArtistCol) else null
                    val bitrate = if (bitrateCol >= 0) cursor.getInt(bitrateCol) else 0
                    val genre = if (genreCol >= 0) cursor.getString(genreCol) ?: "Unknown" else "Unknown"

                    val file = File(path)
                    val parentFile = file.parentFile
                    val folderPath = parentFile?.absolutePath ?: "Unknown"
                    val folderName = parentFile?.name ?: "Unknown"

                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaStoreId).toString()

                    scannedSongs.add(
                        SongEntity(
                            mediaStoreId = mediaStoreId,
                            uri = uri,
                            title = title,
                            artist = artist,
                            albumArtist = albumArtist,
                            album = album,
                            genre = if (genre.isBlank()) "Unknown" else genre,
                            year = year,
                            trackNo = trackNo,
                            discNo = discNo,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            mimeType = mimeType,
                            bitrate = bitrate,
                            albumId = albumId,
                            filePath = path,
                            folderPath = folderPath,
                            folderName = folderName,
                            hidden = false
                        )
                    )
                }
            }

            // Sync with Room:
            val existing = songDao.getExistingSongTimestamps().associate { it.mediaStoreId to it.dateModified }
            val songsToInsert = mutableListOf<SongEntity>()

            for (song in scannedSongs) {
                val existingModified = existing[song.mediaStoreId]
                if (existingModified == null || existingModified != song.dateModified) {
                    songsToInsert.add(song)
                }
            }

            if (songsToInsert.isNotEmpty()) {
                songDao.insertSongs(songsToInsert)
            }

            // Delete songs that no longer exist in MediaStore
            val toDelete = existing.keys.filter { it !in foundMediaStoreIds }
            if (toDelete.isNotEmpty()) {
                songDao.deleteSongsByMediaStoreIds(toDelete)
            }

            AudioScanResult(
                totalFound = scannedSongs.size,
                insertedOrUpdated = songsToInsert.size,
                deleted = toDelete.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AudioScanResult(0, 0, 0, e.message)
        } finally {
            _isScanning.value = false
        }
    }
}

data class AudioScanResult(
    val totalFound: Int,
    val insertedOrUpdated: Int,
    val deleted: Int,
    val error: String? = null
)
