package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "video_audio_preferences",
    indices = [
        Index(value = ["videoPath"], unique = true),
        Index(value = ["folderPath"])
    ]
)
data class VideoAudioPreferenceEntity(
    @PrimaryKey
    val videoId: Long,
    val videoPath: String,
    val folderPath: String,
    val languageCode: String,
    val trackLabel: String,
    val codec: String,
    val channelCount: Int,
    val trackIndex: Int,
    val updatedAtMs: Long = System.currentTimeMillis()
)
