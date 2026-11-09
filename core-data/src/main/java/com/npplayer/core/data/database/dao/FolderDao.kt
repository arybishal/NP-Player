package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY lastPlayedTimestamp DESC, name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ORDER BY lastPlayedTimestamp DESC, name ASC")
    fun searchFolders(query: String): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("DELETE FROM folders")
    suspend fun clearFolders()

    @Query("UPDATE folders SET lastPlayedTimestamp = :timestamp WHERE path = :folderPath")
    suspend fun updateFolderLastPlayed(folderPath: String, timestamp: Long)
}
