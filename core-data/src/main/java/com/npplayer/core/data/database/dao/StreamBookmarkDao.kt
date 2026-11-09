package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.StreamBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamBookmarkDao {
    @Query("SELECT * FROM stream_bookmarks ORDER BY lastPlayedMs DESC")
    fun getAllStreams(): Flow<List<StreamBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: StreamBookmarkEntity): Long

    @Delete
    suspend fun deleteStream(stream: StreamBookmarkEntity)

    @Query("DELETE FROM stream_bookmarks WHERE id = :id")
    suspend fun deleteStreamById(id: Long)
}
