package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_subtitles")
data class DownloadedSubtitleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoPath: String,
    val subtitlePath: String,
    val language: String,
    val languageCode: String,
    val releaseName: String,
    val provider: String = "subdl",
    val downloadedAt: Long = System.currentTimeMillis()
)
