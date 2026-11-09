package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "video_labels",
    primaryKeys = ["videoId", "labelId"],
    indices = [
        Index(value = ["videoId"]),
        Index(value = ["labelId"])
    ]
)
data class VideoLabelCrossRef(
    val videoId: Long,
    val labelId: Long
)
