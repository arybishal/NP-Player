package com.npplayer.core.data.subtitles

data class SubtitleItem(
    val id: String,
    val releaseName: String,
    val language: String,
    val languageCode: String,
    val author: String = "",
    val downloadCount: Int = 0,
    val hearingImpaired: Boolean = false,
    val downloadUrl: String,
    val matchScore: Int = 0,
    val provider: String = "subdl"
)

data class SubtitleSearchQuery(
    val cleanTitle: String,
    val originalFileName: String,
    val queryText: String = "",
    val season: Int? = null,
    val episode: Int? = null,
    val year: Int? = null,
    val type: String = "movie",
    val languages: List<String> = listOf("en"),
    val fileHash: String? = null,
    val fileSize: Long = 0L
)
