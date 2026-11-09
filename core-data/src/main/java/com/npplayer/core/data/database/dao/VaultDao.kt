package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.VaultVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_videos ORDER BY dateAddedMs DESC")
    fun getAllVaultVideos(): Flow<List<VaultVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VaultVideoEntity)

    @Delete
    suspend fun delete(video: VaultVideoEntity)

    @Query("SELECT * FROM vault_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: String): VaultVideoEntity?

    @Query("SELECT COUNT(*) FROM vault_videos")
    fun getVaultCount(): Flow<Int>
}
