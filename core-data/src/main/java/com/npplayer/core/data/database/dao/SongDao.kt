package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.npplayer.core.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE mediaStoreId IN (:mediaStoreIds)")
    suspend fun deleteSongsByMediaStoreIds(mediaStoreIds: List<Long>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>)

    @Query("SELECT * FROM songs WHERE hidden = 0 ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<Long>): List<SongEntity>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%') ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND album = :album ORDER BY trackNo ASC, title ASC")
    fun getSongsByAlbum(album: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND artist = :artist ORDER BY album ASC, trackNo ASC, title ASC")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND genre = :genre ORDER BY title ASC")
    fun getSongsByGenre(genre: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND folderPath = :folderPath ORDER BY title ASC")
    fun getSongsByFolder(folderPath: String): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN music_favorites f ON s.id = f.songId WHERE s.hidden = 0 ORDER BY f.dateLiked DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hidden = 0 AND dateAdded >= :cutoffTimestamp ORDER BY dateAdded DESC")
    fun getRecentlyAddedSongs(cutoffTimestamp: Long): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN music_play_history h ON s.id = h.songId WHERE s.hidden = 0 AND h.lastPlayed > 0 ORDER BY h.lastPlayed DESC")
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN music_play_history h ON s.id = h.songId WHERE s.hidden = 0 AND h.playCount > 0 ORDER BY h.playCount DESC")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s LEFT JOIN music_play_history h ON s.id = h.songId WHERE s.hidden = 0 AND (h.playCount IS NULL OR h.playCount = 0) ORDER BY s.title ASC")
    fun getNeverPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT mediaStoreId, dateModified FROM songs")
    suspend fun getExistingSongTimestamps(): List<SongTimestampTuple>
}

data class SongTimestampTuple(
    val mediaStoreId: Long,
    val dateModified: Long
)
