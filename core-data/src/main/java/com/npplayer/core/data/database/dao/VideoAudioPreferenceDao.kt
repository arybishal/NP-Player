package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.VideoAudioPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoAudioPreferenceDao {
    @Query("SELECT * FROM video_audio_preferences WHERE videoId = :videoId LIMIT 1")
    suspend fun getPreferenceForVideo(videoId: Long): VideoAudioPreferenceEntity?

    @Query("SELECT * FROM video_audio_preferences WHERE videoPath = :videoPath LIMIT 1")
    suspend fun getPreferenceForPath(videoPath: String): VideoAudioPreferenceEntity?

    @Query("SELECT * FROM video_audio_preferences WHERE folderPath = :folderPath ORDER BY updatedAtMs DESC LIMIT 1")
    suspend fun getLatestPreferenceForFolder(folderPath: String): VideoAudioPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(preference: VideoAudioPreferenceEntity)

    @Query("UPDATE video_audio_preferences SET videoPath = :newPath, folderPath = :newFolderPath, updatedAtMs = :timestamp WHERE videoPath = :oldPath")
    suspend fun updatePath(oldPath: String, newPath: String, newFolderPath: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM video_audio_preferences")
    suspend fun clearAllPreferences()

    @Query("DELETE FROM video_audio_preferences WHERE videoPath = :videoPath")
    suspend fun deletePreferenceForPath(videoPath: String)
}
