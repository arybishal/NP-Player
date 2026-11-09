package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_bookmarks")
data class StreamBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val type: String = "HLS", // HLS, DASH, RTSP, HTTP, IPTV
    val lastPlayedMs: Long = System.currentTimeMillis()
)
