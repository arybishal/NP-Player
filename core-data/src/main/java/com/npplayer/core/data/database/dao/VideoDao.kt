package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.npplayer.core.data.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE folderPath = :folderPath ORDER BY dateModified DESC")
    fun getVideosByFolder(folderPath: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY dateModified DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Query("SELECT * FROM videos WHERE path = :path LIMIT 1")
    suspend fun getVideoByPath(path: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET lastPlayedPositionMs = :positionMs, lastPlayedTimestamp = :timestamp WHERE path = :path")
    suspend fun updatePlaybackPosition(path: String, positionMs: Long, timestamp: Long)

    @Query("DELETE FROM videos WHERE path NOT IN (:activePaths)")
    suspend fun pruneDeletedVideos(activePaths: List<String>)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)
}
