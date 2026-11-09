package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.DownloadedSubtitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSubtitleDao {
    @Query("SELECT * FROM downloaded_subtitles WHERE videoPath = :videoPath ORDER BY downloadedAt DESC")
    fun getSubtitlesForVideo(videoPath: String): Flow<List<DownloadedSubtitleEntity>>

    @Query("SELECT * FROM downloaded_subtitles WHERE videoPath = :videoPath ORDER BY downloadedAt DESC")
    suspend fun getSubtitlesForVideoOnce(videoPath: String): List<DownloadedSubtitleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtitle(subtitle: DownloadedSubtitleEntity): Long

    @Query("DELETE FROM downloaded_subtitles WHERE id = :id")
    suspend fun deleteSubtitle(id: Long)
}
