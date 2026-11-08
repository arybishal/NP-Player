package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    indices = [
        Index(value = ["folderPath"]),
        Index(value = ["dateModified"]),
        Index(value = ["title"])
    ]
)
data class VideoEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val path: String,
    val folderPath: String,
    val folderName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val dateModified: Long,
    val lastPlayedPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
)
