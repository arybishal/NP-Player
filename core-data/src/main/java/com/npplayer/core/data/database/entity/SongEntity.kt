package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["folderPath"]),
        Index(value = ["dateAdded"]),
        Index(value = ["dateModified"])
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaStoreId: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val albumArtist: String? = null,
    val album: String,
    val genre: String = "Unknown",
    val year: Int = 0,
    val trackNo: Int = 0,
    val discNo: Int = 0,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val mimeType: String,
    val bitrate: Int = 0,
    val albumId: Long = 0L,
    val filePath: String = "",
    val folderPath: String,
    val folderName: String,
    val hidden: Boolean = false
)
