package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_videos")
data class VaultVideoEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val vaultPath: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedMs: Long = System.currentTimeMillis(),
    val mimeType: String = "video/*"
)
