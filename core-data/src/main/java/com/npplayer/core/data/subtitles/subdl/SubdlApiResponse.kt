package com.npplayer.core.data.subtitles.subdl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubdlApiResponse(
    val status: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val results: List<SubdlSubtitleDto>? = null,
    val subtitles: List<SubdlSubtitleDto>? = null
)

@Serializable
data class SubdlSubtitleDto(
    @SerialName("release_name")
    val releaseName: String? = null,
    val name: String? = null,
    val lang: String? = null,
    val language: String? = null,
    val author: String? = null,
    val url: String? = null,
    val episode: Int? = null,
    val season: Int? = null,
    val year: Int? = null,
    val downloads: Int? = null,
    val hi: Boolean? = null
)
