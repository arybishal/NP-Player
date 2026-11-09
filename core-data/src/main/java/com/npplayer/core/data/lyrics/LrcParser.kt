package com.npplayer.core.data.lyrics

import java.util.regex.Pattern

data class WordTimestamp(
    val timestampMs: Long,
    val word: String
)

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val words: List<WordTimestamp> = emptyList()
)

data class ParsedLyrics(
    val lines: List<LyricLine>,
    val offsetMs: Long = 0L,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null
)

object LrcParser {

    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
    private val OFFSET_PATTERN = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\]", Pattern.CASE_INSENSITIVE)
    private val TITLE_PATTERN = Pattern.compile("\\[ti:\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE)
    private val ARTIST_PATTERN = Pattern.compile("\\[ar:\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE)
    private val ALBUM_PATTERN = Pattern.compile("\\[al:\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE)
    private val WORD_TAG_PATTERN = Pattern.compile("<(\\d{1,2}):(\\d{2})(?:[.:](\\d{2,3}))?>")

    fun parse(lrcContent: String): ParsedLyrics {
        val lines = lrcContent.lines()
        val lyricLines = mutableListOf<LyricLine>()
        var globalOffsetMs = 0L
        var title: String? = null
        var artist: String? = null
        var album: String? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            // Parse metadata
            val offsetMatcher = OFFSET_PATTERN.matcher(line)
            if (offsetMatcher.find()) {
                globalOffsetMs = offsetMatcher.group(1)?.toLongOrNull() ?: 0L
                continue
            }
            val titleMatcher = TITLE_PATTERN.matcher(line)
            if (titleMatcher.find()) {
                title = titleMatcher.group(1)?.trim()
                continue
            }
            val artistMatcher = ARTIST_PATTERN.matcher(line)
            if (artistMatcher.find()) {
                artist = artistMatcher.group(1)?.trim()
                continue
            }
            val albumMatcher = ALBUM_PATTERN.matcher(line)
            if (albumMatcher.find()) {
                album = albumMatcher.group(1)?.trim()
                continue
            }

            // Find all time tags on the line e.g. [01:23.45][02:34.56]Text
            val timeMatcher = TIME_TAG_PATTERN.matcher(line)
            val timestamps = mutableListOf<Long>()
            var lastTagEnd = 0

            while (timeMatcher.find()) {
                val min = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val sec = timeMatcher.group(2)?.toLongOrNull() ?: 0L
                val msGroup = timeMatcher.group(3)
                val ms = when {
                    msGroup == null -> 0L
                    msGroup.length == 2 -> (msGroup.toLongOrNull() ?: 0L) * 10
                    else -> msGroup.toLongOrNull() ?: 0L
                }
                val totalMs = (min * 60 + sec) * 1000 + ms
                timestamps.add(totalMs)
                lastTagEnd = timeMatcher.end()
            }

            if (timestamps.isNotEmpty()) {
                val textWithWords = line.substring(lastTagEnd).trim()
                // Parse word timestamps if present e.g. <00:01.00>hello <00:01.50>world
                val (plainText, words) = parseWordTimestamps(textWithWords)

                for (ts in timestamps) {
                    lyricLines.add(
                        LyricLine(
                            timestampMs = (ts + globalOffsetMs).coerceAtLeast(0L),
                            text = plainText,
                            words = words
                        )
                    )
                }
            }
        }

        val sortedLines = lyricLines.sortedBy { it.timestampMs }
        return ParsedLyrics(
            lines = sortedLines,
            offsetMs = globalOffsetMs,
            title = title,
            artist = artist,
            album = album
        )
    }

    private fun parseWordTimestamps(rawText: String): Pair<String, List<WordTimestamp>> {
        val wordMatcher = WORD_TAG_PATTERN.matcher(rawText)
        if (!wordMatcher.find()) {
            return Pair(rawText, emptyList())
        }

        wordMatcher.reset()
        val words = mutableListOf<WordTimestamp>()
        val plainTextBuilder = StringBuilder()
        var lastIndex = 0

        var currentTs = 0L
        while (wordMatcher.find()) {
            val min = wordMatcher.group(1)?.toLongOrNull() ?: 0L
            val sec = wordMatcher.group(2)?.toLongOrNull() ?: 0L
            val msGroup = wordMatcher.group(3)
            val ms = when {
                msGroup == null -> 0L
                msGroup.length == 2 -> (msGroup.toLongOrNull() ?: 0L) * 10
                else -> msGroup.toLongOrNull() ?: 0L
            }
            val wordText = rawText.substring(lastIndex, wordMatcher.start()).trim()
            if (wordText.isNotEmpty() && currentTs > 0L) {
                words.add(WordTimestamp(currentTs, wordText))
                plainTextBuilder.append(wordText).append(" ")
            }
            currentTs = (min * 60 + sec) * 1000 + ms
            lastIndex = wordMatcher.end()
        }

        val remaining = rawText.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            if (currentTs > 0L) {
                words.add(WordTimestamp(currentTs, remaining))
            }
            plainTextBuilder.append(remaining)
        }

        val finalPlain = plainTextBuilder.toString().trim()
        return Pair(if (finalPlain.isEmpty()) rawText else finalPlain, words)
    }

    fun findActiveLineIndex(lines: List<LyricLine>, currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        if (currentPositionMs < lines.first().timestampMs) return 0
        for (i in lines.indices.reversed()) {
            if (currentPositionMs >= lines[i].timestampMs) {
                return i
            }
        }
        return lines.size - 1
    }
}
