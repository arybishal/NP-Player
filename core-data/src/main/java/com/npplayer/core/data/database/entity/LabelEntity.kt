package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "labels",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtMs: Long = System.currentTimeMillis()
)
