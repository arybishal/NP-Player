package com.npplayer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "music_playlists",
    indices = [
        Index(value = ["name"])
    ]
)
data class MusicPlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false,
    val smartType: String? = null
)

@Entity(
    tableName = "music_playlist_songs",
    primaryKeys = ["playlistId", "songId", "orderIndex"],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["songId"])
    ]
)
data class MusicPlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)
