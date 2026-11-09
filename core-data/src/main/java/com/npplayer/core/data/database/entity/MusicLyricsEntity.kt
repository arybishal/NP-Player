package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "music_lyrics",
    indices = [
        Index(value = ["songId"], unique = true),
        Index(value = ["trackName", "artistName"])
    ]
)
data class MusicLyricsEntity(
    @PrimaryKey
    val songId: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val isInstrumental: Boolean = false,
    val source: String = "lrclib", // "embedded", "sidecar", "lrclib", "none"
    val offsetMs: Long = 0L,
    val lastFetched: Long = System.currentTimeMillis()
)
