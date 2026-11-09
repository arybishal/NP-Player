package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_queue_state")
data class MusicQueueStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val queueSongIds: String = "",
    val currentIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0
)
