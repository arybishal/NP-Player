package com.npplayer.core.data.subtitles

import java.io.File

interface SubtitleProvider {
    val id: String
    val displayName: String

    suspend fun search(query: SubtitleSearchQuery): Result<List<SubtitleItem>>

    suspend fun download(
        item: SubtitleItem,
        targetDirectory: File,
        preferredBaseName: String
    ): Result<File>
}
