package com.npplayer.core.data.repository

import com.npplayer.core.data.database.dao.FavoriteSongDao
import com.npplayer.core.data.database.dao.MusicPlayHistoryDao
import com.npplayer.core.data.database.dao.MusicPlaylistDao
import com.npplayer.core.data.database.dao.SongDao
import com.npplayer.core.data.database.entity.FavoriteSongEntity
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.MusicPlaylistSongCrossRef
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.scanner.AudioMediaStoreScanner
import com.npplayer.core.data.scanner.AudioScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class MusicAlbum(
    val name: String,
    val artist: String,
    val year: Int,
    val trackCount: Int,
    val albumId: Long,
    val dateAdded: Long
)

data class MusicArtist(
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val firstAlbumId: Long
)

data class MusicGenre(
    val name: String,
    val songCount: Int
)

data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int
)

data class MusicSearchResult(
    val query: String,
    val songs: List<SongEntity>,
    val albums: List<MusicAlbum>,
    val artists: List<MusicArtist>,
    val playlists: List<MusicPlaylistEntity>
)

enum class SongSortOption {
    TITLE, ARTIST, ALBUM, TRACK_NUMBER, DURATION, DATE_ADDED, YEAR, PLAY_COUNT, SIZE
}

enum class AlbumSortOption {
    NAME, ARTIST, YEAR, DATE_ADDED, TRACK_COUNT
}

enum class ArtistSortOption {
    NAME, ALBUMS_COUNT, SONGS_COUNT
}

enum class GenreSortOption {
    NAME, SONGS_COUNT
}

enum class PlaylistSortOption {
    NAME, DATE_CREATED, SONGS_COUNT
}

enum class FolderSortOption {
    NAME, SONGS_COUNT
}

enum class FavoriteSortOption {
    DATE_LIKED, TITLE, ARTIST
}

enum class RecentlyAddedFilter(val days: Int) {
    DAYS_7(7),
    DAYS_14(14),
    DAYS_30(30),
    DAYS_90(90)
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

@Singleton
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: MusicPlaylistDao,
    private val favoriteDao: FavoriteSongDao,
    private val historyDao: MusicPlayHistoryDao,
    private val audioScanner: AudioMediaStoreScanner
) {
    val isScanning: Flow<Boolean> = audioScanner.isScanning

    suspend fun scanAudio(): AudioScanResult = audioScanner.scanAudio()

    fun getAllSongs(
        sortOption: SongSortOption = SongSortOption.TITLE,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<SongEntity>> {
        return combine(songDao.getAllSongsFlow(), historyDao.getAllHistory()) { songs, histories ->
            val historyMap = histories.associate { it.songId to it.playCount }

            val sorted = when (sortOption) {
                SongSortOption.TITLE -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                SongSortOption.ARTIST -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist })
                SongSortOption.ALBUM -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.album })
                SongSortOption.TRACK_NUMBER -> songs.sortedBy { it.trackNo }
                SongSortOption.DURATION -> songs.sortedBy { it.durationMs }
                SongSortOption.DATE_ADDED -> songs.sortedBy { it.dateAdded }
                SongSortOption.YEAR -> songs.sortedBy { it.year }
                SongSortOption.PLAY_COUNT -> songs.sortedBy { historyMap[it.id] ?: 0 }
                SongSortOption.SIZE -> songs.sortedBy { it.sizeBytes }
            }

            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getAlbums(
        sortOption: AlbumSortOption = AlbumSortOption.NAME,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<MusicAlbum>> {
        return songDao.getAllSongsFlow().map { songs ->
            val grouped = songs.groupBy { it.album }
            val albums = grouped.map { (albumName, albumSongs) ->
                val first = albumSongs.first()
                MusicAlbum(
                    name = albumName,
                    artist = first.albumArtist ?: first.artist,
                    year = albumSongs.maxOfOrNull { it.year } ?: 0,
                    trackCount = albumSongs.size,
                    albumId = first.albumId,
                    dateAdded = albumSongs.maxOfOrNull { it.dateAdded } ?: 0L
                )
            }

            val sorted = when (sortOption) {
                AlbumSortOption.NAME -> albums.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                AlbumSortOption.ARTIST -> albums.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist })
                AlbumSortOption.YEAR -> albums.sortedBy { it.year }
                AlbumSortOption.DATE_ADDED -> albums.sortedBy { it.dateAdded }
                AlbumSortOption.TRACK_COUNT -> albums.sortedBy { it.trackCount }
            }

            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getArtists(
        sortOption: ArtistSortOption = ArtistSortOption.NAME,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<MusicArtist>> {
        return songDao.getAllSongsFlow().map { songs ->
            val grouped = songs.groupBy { it.artist }
            val artists = grouped.map { (artistName, artistSongs) ->
                val distinctAlbums = artistSongs.map { it.album }.distinct().size
                val firstAlbumId = artistSongs.firstOrNull { it.albumId != 0L }?.albumId ?: 0L
                MusicArtist(
                    name = artistName,
                    albumCount = distinctAlbums,
                    songCount = artistSongs.size,
                    firstAlbumId = firstAlbumId
                )
            }

            val sorted = when (sortOption) {
                ArtistSortOption.NAME -> artists.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                ArtistSortOption.ALBUMS_COUNT -> artists.sortedBy { it.albumCount }
                ArtistSortOption.SONGS_COUNT -> artists.sortedBy { it.songCount }
            }

            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getGenres(
        sortOption: GenreSortOption = GenreSortOption.NAME,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<MusicGenre>> {
        return songDao.getAllSongsFlow().map { songs ->
            val grouped = songs.groupBy { it.genre }
            val genres = grouped.map { (genreName, genreSongs) ->
                MusicGenre(
                    name = genreName,
                    songCount = genreSongs.size
                )
            }

            val sorted = when (sortOption) {
                GenreSortOption.NAME -> genres.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                GenreSortOption.SONGS_COUNT -> genres.sortedBy { it.songCount }
            }

            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getFolders(
        sortOption: FolderSortOption = FolderSortOption.NAME,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<MusicFolder>> {
        return songDao.getAllSongsFlow().map { songs ->
            val grouped = songs.groupBy { it.folderPath }
            val folders = grouped.map { (folderPath, folderSongs) ->
                val folderName = folderSongs.first().folderName
                MusicFolder(
                    path = folderPath,
                    name = folderName,
                    songCount = folderSongs.size
                )
            }

            val sorted = when (sortOption) {
                FolderSortOption.NAME -> folders.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                FolderSortOption.SONGS_COUNT -> folders.sortedBy { it.songCount }
            }

            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getFavorites(
        sortOption: FavoriteSortOption = FavoriteSortOption.DATE_LIKED,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<SongEntity>> {
        return songDao.getFavoriteSongs().map { songs ->
            val sorted = when (sortOption) {
                FavoriteSortOption.DATE_LIKED -> songs // Default DAO order is dateLiked DESC
                FavoriteSortOption.TITLE -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                FavoriteSortOption.ARTIST -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist })
            }
            if (direction == SortDirection.DESCENDING) {
                if (sortOption == FavoriteSortOption.DATE_LIKED) sorted else sorted.reversed()
            } else {
                if (sortOption == FavoriteSortOption.DATE_LIKED) sorted.reversed() else sorted
            }
        }.flowOn(Dispatchers.Default)
    }

    fun getRecentlyAdded(
        range: RecentlyAddedFilter = RecentlyAddedFilter.DAYS_30,
        direction: SortDirection = SortDirection.DESCENDING
    ): Flow<List<SongEntity>> {
        val cutoffSeconds = (System.currentTimeMillis() / 1000) - TimeUnit.DAYS.toSeconds(range.days.toLong())
        return songDao.getRecentlyAddedSongs(cutoffSeconds).map { songs ->
            if (direction == SortDirection.ASCENDING) songs.reversed() else songs
        }.flowOn(Dispatchers.Default)
    }

    fun getPlaylists(
        sortOption: PlaylistSortOption = PlaylistSortOption.NAME,
        direction: SortDirection = SortDirection.ASCENDING
    ): Flow<List<MusicPlaylistEntity>> {
        return playlistDao.getAllPlaylists().map { playlists ->
            val sorted = when (sortOption) {
                PlaylistSortOption.NAME -> playlists.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                PlaylistSortOption.DATE_CREATED -> playlists.sortedBy { it.createdAt }
                PlaylistSortOption.SONGS_COUNT -> playlists // Updated when loaded
            }
            if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
        }.flowOn(Dispatchers.Default)
    }

    fun getSongsByAlbum(album: String): Flow<List<SongEntity>> = songDao.getSongsByAlbum(album)

    fun getSongsByArtist(artist: String): Flow<List<SongEntity>> = songDao.getSongsByArtist(artist)

    fun getSongsByGenre(genre: String): Flow<List<SongEntity>> = songDao.getSongsByGenre(genre)

    fun getSongsByFolder(folderPath: String): Flow<List<SongEntity>> = songDao.getSongsByFolder(folderPath)

    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>> = playlistDao.getSongsForPlaylist(playlistId)

    suspend fun getSongById(id: Long): SongEntity? = songDao.getSongById(id)

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    fun isFavoriteFlow(songId: Long): Flow<Boolean> = favoriteDao.isFavoriteFlow(songId)

    fun getAllFavoriteIds(): Flow<List<Long>> = favoriteDao.getAllFavoriteSongIds()

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        if (favoriteDao.isFavorite(songId)) {
            favoriteDao.deleteFavorite(songId)
        } else {
            favoriteDao.insertFavorite(FavoriteSongEntity(songId = songId))
        }
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(MusicPlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        val existing = playlistDao.getPlaylistById(playlistId) ?: return@withContext
        playlistDao.updatePlaylist(existing.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
        playlistDao.clearPlaylistSongs(playlistId)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        val refs = songIds.mapIndexed { index, sId ->
            MusicPlaylistSongCrossRef(playlistId = playlistId, songId = sId, orderIndex = index)
        }
        playlistDao.insertPlaylistSongs(refs)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun reorderPlaylistTracks(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.replacePlaylistTracks(playlistId, songIds)
    }

    fun getRecentlyPlayed(): Flow<List<SongEntity>> = songDao.getRecentlyPlayedSongs()
    fun getMostPlayed(): Flow<List<SongEntity>> = songDao.getMostPlayedSongs()
    fun getNeverPlayed(): Flow<List<SongEntity>> = songDao.getNeverPlayedSongs()

    suspend fun exportPlaylistToM3u(playlistId: Long): String? = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return@withContext null
        val songs = playlistDao.getSongsForPlaylist(playlistId).firstOrNull() ?: emptyList()
        M3uPlaylistHelper.generateM3u(playlist.name, songs)
    }

    suspend fun importPlaylistFromM3u(name: String, m3uContent: String): Long = withContext(Dispatchers.IO) {
        val entries = M3uPlaylistHelper.parseM3u(m3uContent)
        val allSongs = songDao.getAllSongs()
        val matchedIds = mutableListOf<Long>()

        for (entry in entries) {
            val match = allSongs.firstOrNull { song ->
                song.filePath.equals(entry.pathOrUri, ignoreCase = true) ||
                (entry.title.isNotBlank() && song.title.contains(entry.title, ignoreCase = true))
            }
            if (match != null) {
                matchedIds.add(match.id)
            }
        }

        val playlistId = playlistDao.insertPlaylist(MusicPlaylistEntity(name = name))
        if (matchedIds.isNotEmpty()) {
            addSongsToPlaylist(playlistId, matchedIds)
        }
        playlistId
    }

    suspend fun hideSong(songId: Long, hidden: Boolean = true) = withContext(Dispatchers.IO) {
        val song = songDao.getSongById(songId) ?: return@withContext
        songDao.updateSong(song.copy(hidden = hidden))
    }

    suspend fun deleteSong(songId: Long) = withContext(Dispatchers.IO) {
        songDao.deleteSongsByIds(listOf(songId))
    }

    suspend fun deleteSongs(songIds: List<Long>) = withContext(Dispatchers.IO) {
        songDao.deleteSongsByIds(songIds)
    }

    suspend fun updateSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.updateSong(song)
    }

    fun searchMusic(query: String): Flow<MusicSearchResult> {
        val cleanQuery = query.trim()
        return songDao.searchSongs(cleanQuery).combine(playlistDao.getAllPlaylists()) { songs, playlists ->
            val matchingAlbums = songs.groupBy { it.album }.map { (albumName, albumSongs) ->
                val first = albumSongs.first()
                MusicAlbum(
                    name = albumName,
                    artist = first.albumArtist ?: first.artist,
                    year = albumSongs.maxOfOrNull { it.year } ?: 0,
                    trackCount = albumSongs.size,
                    albumId = first.albumId,
                    dateAdded = albumSongs.maxOfOrNull { it.dateAdded } ?: 0L
                )
            }

            val matchingArtists = songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
                MusicArtist(
                    name = artistName,
                    albumCount = artistSongs.map { it.album }.distinct().size,
                    songCount = artistSongs.size,
                    firstAlbumId = artistSongs.firstOrNull { it.albumId != 0L }?.albumId ?: 0L
                )
            }

            val matchingPlaylists = if (cleanQuery.isBlank()) emptyList() else {
                playlists.filter { it.name.contains(cleanQuery, ignoreCase = true) }
            }

            MusicSearchResult(
                query = cleanQuery,
                songs = songs,
                albums = matchingAlbums,
                artists = matchingArtists,
                playlists = matchingPlaylists
            )
        }.flowOn(Dispatchers.Default)
    }
}
