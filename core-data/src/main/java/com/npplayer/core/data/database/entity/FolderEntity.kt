package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val videoCount: Int,
    val totalSizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val newVideoCount: Int = 0
)
