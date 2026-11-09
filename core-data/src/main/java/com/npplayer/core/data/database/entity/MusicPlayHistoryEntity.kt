package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_play_history")
data class MusicPlayHistoryEntity(
    @PrimaryKey
    val songId: Long,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
)
