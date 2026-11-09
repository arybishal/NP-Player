package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_favorites")
data class FavoriteSongEntity(
    @PrimaryKey
    val songId: Long,
    val dateLiked: Long = System.currentTimeMillis()
)
