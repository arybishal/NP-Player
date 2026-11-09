package com.npplayer.core.data.subtitles

import java.io.File
import java.util.regex.Pattern

data class CleanedMediaInfo(
    val cleanTitle: String,
    val season: Int? = null,
    val episode: Int? = null,
    val year: Int? = null,
    val type: String = "movie",
    val originalFileName: String = "",
    val suggestedQuery: String = ""
)

object SubtitleFilenameCleaner {

    private val EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "ts", "3gp", "vob"
    )

    private val TV_PATTERN = Pattern.compile(
        "(?i)\\b(?:s(\\d{1,2})e(\\d{1,2})|(\\d{1,2})x(\\d{1,2})|season\\s*(\\d{1,2})\\s*episode\\s*(\\d{1,2}))\\b"
    )

    private val ANIME_EPISODE_PATTERN = Pattern.compile(
        "(?i)(?:\\s+-\\s+|\\s+ep\\.?\\s*|\\s+episode\\s*)(\\d{1,3})\\b"
    )

    private val YEAR_PATTERN = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b")

    private val TAGS_PATTERN = Pattern.compile(
        "(?i)\\b(2160p|4k|1080p|1080i|720p|480p|576p|uhd|hdr|hdr10|dovi|dv|remux|bluray|blu-ray|bdrip|brrip|web-dl|webrip|web|hdtv|hdrip|dvdrip|cam|telesync|ts|tc|r5|" +
                "x264|x265|h264|h265|hevc|avc|av1|xvid|divx|10bit|8bit|60fps|" +
                "aac|ac3|eac3|dts|dts-hd|truehd|ddp5\\.1|dd5\\.1|atmos|mp3|flac|stereo|5\\.1|7\\.1|" +
                "yts|yify|rarbg|psa|sparks|geckos|dimension|eztv|ettv|rartv|evo|galaxytv|tgx)\\b"
    )

    fun clean(rawNameOrPath: String): CleanedMediaInfo {
        var name = File(rawNameOrPath).name

        // Strip file extension
        val dotIndex = name.lastIndexOf('.')
        if (dotIndex != -1 && name.substring(dotIndex + 1).lowercase() in EXTENSIONS) {
            name = name.substring(0, dotIndex)
        }

        val originalFileName = name

        // Pre-normalize dots and underscores into spaces for accurate word boundary regexes
        var working = name.replace(Regex("\\[.*?\\]"), " ")
            .replace(Regex("\\(.*?\\)"), " ")
            .replace(Regex("[._]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Detect TV Season & Episode
        var season: Int? = null
        var episode: Int? = null
        val tvMatcher = TV_PATTERN.matcher(working)
        val animeMatcher = ANIME_EPISODE_PATTERN.matcher(working)

        var titlePart = working
        if (tvMatcher.find()) {
            season = tvMatcher.group(1)?.toIntOrNull()
                ?: tvMatcher.group(3)?.toIntOrNull()
                ?: tvMatcher.group(5)?.toIntOrNull()
            episode = tvMatcher.group(2)?.toIntOrNull()
                ?: tvMatcher.group(4)?.toIntOrNull()
                ?: tvMatcher.group(6)?.toIntOrNull()
            titlePart = working.substring(0, tvMatcher.start()).trim()
        } else if (animeMatcher.find()) {
            episode = animeMatcher.group(1)?.toIntOrNull()
            titlePart = working.substring(0, animeMatcher.start()).trim()
        }

        // Detect Year
        var year: Int? = null
        val yearMatcher = YEAR_PATTERN.matcher(working)
        if (yearMatcher.find()) {
            year = yearMatcher.group(1)?.toIntOrNull()
            if (season == null && episode == null && yearMatcher.start() > 2) {
                val candidateTitle = working.substring(0, yearMatcher.start()).trim()
                if (candidateTitle.isNotBlank()) {
                    titlePart = candidateTitle
                }
            }
        }

        // Clean out leftover tags, hyphens, and punctuation
        var clean = titlePart.replace(Regex("[-_]"), " ")
        clean = TAGS_PATTERN.matcher(clean).replaceAll(" ")
        clean = clean.replace(Regex("\\s+-\\s*$"), "") // remove trailing dash
        clean = clean.replace(Regex("\\s+"), " ").trim()

        if (clean.isBlank()) {
            clean = working
        }

        val isTv = season != null || episode != null
        val type = if (isTv) "tv" else "movie"

        val formattedSeasonEp = if (season != null && episode != null) {
            String.format("S%02dE%02d", season, episode)
        } else if (episode != null) {
            String.format("E%02d", episode)
        } else ""

        val suggestedQuery = buildString {
            append(clean)
            if (formattedSeasonEp.isNotEmpty()) {
                append(" ").append(formattedSeasonEp)
            } else if (year != null) {
                append(" ").append(year)
            }
        }.trim()

        return CleanedMediaInfo(
            cleanTitle = clean,
            season = season,
            episode = episode,
            year = year,
            type = type,
            originalFileName = originalFileName,
            suggestedQuery = suggestedQuery
        )
    }
}
