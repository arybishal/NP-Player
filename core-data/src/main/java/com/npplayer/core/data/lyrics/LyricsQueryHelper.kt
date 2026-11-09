package com.npplayer.core.data.lyrics

object LyricsQueryHelper {

    private val STRIP_PATTERNS = listOf(
        "\\(official\\s*(?:music\\s*)?video\\)",
        "\\(official\\s*audio\\)",
        "\\[official\\s*(?:music\\s*)?video\\]",
        "\\[official\\s*audio\\]",
        "\\(lyrics\\)",
        "\\[lyrics\\]",
        "\\(lyric\\s*video\\)",
        "\\[lyric\\s*video\\]",
        "\\(audio\\)",
        "\\[audio\\]",
        "\\(visualizer\\)",
        "\\[visualizer\\]",
        "\\(remastered.*?\\)",
        "\\[remastered.*?\\]",
        "\\(hq\\)",
        "\\[hq\\]",
        "\\(hd\\)",
        "\\[hd\\]",
        "\\(4k\\)",
        "\\[4k\\]"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    private val TRACK_NUMBER_PREFIX = Regex("^\\d{1,3}[.\\-\\s_]+", RegexOption.IGNORE_CASE)
    private val FEATURING_PATTERN = Regex("\\s*[\\(\\[]\\s*(?:feat|ft|featuring)\\.?\\s+.*?[\\)\\]]|\\s+(?:feat|ft|featuring)\\.?\\s+.*", RegexOption.IGNORE_CASE)

    fun cleanTitle(rawTitle: String): String {
        var title = rawTitle.trim()

        // Strip track number prefixes like "01. ", "01 - "
        title = title.replace(TRACK_NUMBER_PREFIX, "")

        // Strip video/audio tags
        for (pattern in STRIP_PATTERNS) {
            title = title.replace(pattern, "")
        }

        // Strip featuring info if inside title e.g. "Song (feat. Artist)"
        title = title.replace(FEATURING_PATTERN, "")

        return title.trim()
    }

    fun normalizeArtist(rawArtist: String): String {
        var artist = rawArtist.trim()

        // If artist has "feat.", split and take first
        val featSplit = artist.split(Regex("\\s+(?:feat|ft|featuring)\\.?\\s+", RegexOption.IGNORE_CASE))
        if (featSplit.isNotEmpty()) {
            artist = featSplit[0].trim()
        }

        // If multiple artists separated by comma, slash, or ampersand
        val delimiterSplit = artist.split(Regex("[,/&]"))
        if (delimiterSplit.isNotEmpty()) {
            artist = delimiterSplit[0].trim()
        }

        return if (artist.isBlank() || artist.equals("<unknown>", ignoreCase = true)) "" else artist
    }
}
