package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.MusicPlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicPlayHistoryDao {

    @Query("SELECT * FROM music_play_history WHERE songId = :songId LIMIT 1")
    suspend fun getHistory(songId: Long): MusicPlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MusicPlayHistoryEntity)

    @Query("SELECT * FROM music_play_history")
    fun getAllHistory(): Flow<List<MusicPlayHistoryEntity>>

    @Query("UPDATE music_play_history SET playCount = playCount + 1, lastPlayed = :timestamp WHERE songId = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long): Int

    suspend fun recordPlay(songId: Long, timestamp: Long = System.currentTimeMillis()) {
        val updatedRows = incrementPlayCount(songId, timestamp)
        if (updatedRows == 0) {
            insertHistory(
                MusicPlayHistoryEntity(
                    songId = songId,
                    playCount = 1,
                    lastPlayed = timestamp
                )
            )
        }
    }
}
