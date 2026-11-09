package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.LabelEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.database.entity.VideoLabelCrossRef
import kotlinx.coroutines.flow.Flow

data class LabelWithCount(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val videoCount: Int
)

@Dao
interface LabelDao {
    @Query("""
        SELECT l.id, l.name, l.createdAtMs, COUNT(vl.videoId) AS videoCount
        FROM labels l
        LEFT JOIN video_labels vl ON l.id = vl.labelId
        GROUP BY l.id
        ORDER BY l.name ASC
    """)
    fun getAllLabelsWithCount(): Flow<List<LabelWithCount>>

    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<LabelEntity>>

    @Query("""
        SELECT l.* FROM labels l
        INNER JOIN video_labels vl ON l.id = vl.labelId
        WHERE vl.videoId = :videoId
        ORDER BY l.name ASC
    """)
    fun getLabelsForVideo(videoId: Long): Flow<List<LabelEntity>>

    @Query("""
        SELECT * FROM video_labels
    """)
    fun getAllCrossRefs(): Flow<List<VideoLabelCrossRef>>

    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN video_labels vl ON v.id = vl.videoId
        WHERE vl.labelId = :labelId
        ORDER BY v.dateModified DESC
    """)
    fun getVideosForLabel(labelId: Long): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: LabelEntity): Long

    @Query("SELECT * FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): LabelEntity?

    @Query("DELETE FROM labels WHERE id = :labelId")
    suspend fun deleteLabel(labelId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabelToVideo(crossRef: VideoLabelCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabelsToVideos(crossRefs: List<VideoLabelCrossRef>)

    @Query("DELETE FROM video_labels WHERE videoId = :videoId AND labelId = :labelId")
    suspend fun removeLabelFromVideo(videoId: Long, labelId: Long)

    @Query("DELETE FROM video_labels WHERE videoId IN (:videoIds) AND labelId = :labelId")
    suspend fun removeLabelFromVideos(videoIds: List<Long>, labelId: Long)

    @Query("DELETE FROM video_labels WHERE videoId = :videoId")
    suspend fun clearLabelsForVideo(videoId: Long)
}
