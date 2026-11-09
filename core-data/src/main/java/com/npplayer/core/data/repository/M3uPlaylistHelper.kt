package com.npplayer.core.data.repository

import com.npplayer.core.data.database.entity.SongEntity
import java.io.BufferedReader
import java.io.StringReader

object M3uPlaylistHelper {

    fun generateM3u(playlistName: String, songs: List<SongEntity>): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#PLAYLIST:").append(playlistName).append("\n")
        for (song in songs) {
            val durationSec = song.durationMs / 1000
            val artistTitle = if (song.artist.isNotBlank()) "${song.artist} - ${song.title}" else song.title
            sb.append("#EXTINF:").append(durationSec).append(",").append(artistTitle).append("\n")
            sb.append(song.filePath).append("\n")
        }
        return sb.toString()
    }

    data class ParsedM3uEntry(
        val durationSec: Long,
        val title: String,
        val pathOrUri: String
    )

    fun parseM3u(content: String): List<ParsedM3uEntry> {
        val reader = BufferedReader(StringReader(content))
        val results = mutableListOf<ParsedM3uEntry>()
        var currentDuration = 0L
        var currentTitle = ""

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachLine

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                val metadata = line.removePrefix("#EXTINF:").trim()
                val commaIndex = metadata.indexOf(',')
                if (commaIndex != -1) {
                    val durStr = metadata.substring(0, commaIndex).trim()
                    currentDuration = durStr.toDoubleOrNull()?.toLong() ?: 0L
                    currentTitle = metadata.substring(commaIndex + 1).trim()
                } else {
                    currentDuration = metadata.toDoubleOrNull()?.toLong() ?: 0L
                }
            } else if (!line.startsWith("#")) {
                results.add(
                    ParsedM3uEntry(
                        durationSec = currentDuration,
                        title = currentTitle,
                        pathOrUri = line
                    )
                )
                currentDuration = 0L
                currentTitle = ""
            }
        }
        return results
    }
}
