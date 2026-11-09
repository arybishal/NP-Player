package com.npplayer.core.data.lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LrclibResponse(
    val id: Long? = null,
    val name: String? = null,
    @SerialName("trackName")
    val trackName: String? = null,
    @SerialName("artistName")
    val artistName: String? = null,
    @SerialName("albumName")
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean? = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)
