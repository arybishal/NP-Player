package com.npplayer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.dao.WatchHistoryDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.database.entity.WatchHistoryEntity

@Database(
    entities = [
        VideoEntity::class,
        FolderEntity::class,
        WatchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NPPlayerDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun folderDao(): FolderDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
