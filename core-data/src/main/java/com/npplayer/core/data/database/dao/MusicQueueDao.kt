package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.MusicQueueStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicQueueDao {

    @Query("SELECT * FROM music_queue_state WHERE id = 1 LIMIT 1")
    fun getQueueStateFlow(): Flow<MusicQueueStateEntity?>

    @Query("SELECT * FROM music_queue_state WHERE id = 1 LIMIT 1")
    suspend fun getQueueState(): MusicQueueStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQueueState(state: MusicQueueStateEntity)
}
