package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.PlaylistEntity
import com.npplayer.core.data.database.entity.PlaylistItemEntity
import com.npplayer.core.data.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAtMs DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeItem(playlistId: Long, videoId: Long)

    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN playlist_items pi ON v.id = pi.videoId
        WHERE pi.playlistId = :playlistId
        ORDER BY pi.orderIndex ASC
    """)
    fun getVideosForPlaylist(playlistId: Long): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getItemCount(playlistId: Long): Int

    @Query("UPDATE playlists SET videoCount = :count WHERE id = :playlistId")
    suspend fun updateCount(playlistId: Long, count: Int)
}
