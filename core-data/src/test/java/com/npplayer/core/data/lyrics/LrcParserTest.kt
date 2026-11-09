package com.npplayer.core.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun parseStandardLrc_parsesMetadataAndTimestamps() {
        val lrc = """
            [ti:Test Title]
            [ar:Test Artist]
            [al:Test Album]
            [offset:500]
            [00:01.00]First line
            [00:05.50]Second line
            [01:10.250]Third line
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals("Test Title", parsed.title)
        assertEquals("Test Artist", parsed.artist)
        assertEquals("Test Album", parsed.album)
        assertEquals(500L, parsed.offsetMs)
        assertEquals(3, parsed.lines.size)

        // 00:01.00 = 1000ms + 500ms offset = 1500ms
        assertEquals(1500L, parsed.lines[0].timestampMs)
        assertEquals("First line", parsed.lines[0].text)

        // 00:05.50 = 5500ms + 500ms offset = 6000ms
        assertEquals(6000L, parsed.lines[1].timestampMs)
        assertEquals("Second line", parsed.lines[1].text)

        // 01:10.250 = 70250ms + 500ms offset = 70750ms
        assertEquals(70750L, parsed.lines[2].timestampMs)
        assertEquals("Third line", parsed.lines[2].text)
    }

    @Test
    fun parseMultipleTimestampsOnSingleLine() {
        val lrc = """
            [00:10.00][00:20.00]Chorus line repeated
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals(2, parsed.lines.size)
        assertEquals(10000L, parsed.lines[0].timestampMs)
        assertEquals("Chorus line repeated", parsed.lines[0].text)
        assertEquals(20000L, parsed.lines[1].timestampMs)
        assertEquals("Chorus line repeated", parsed.lines[1].text)
    }

    @Test
    fun parseWordLevelTimestamps() {
        val lrc = """
            [00:05.00]<00:05.00>Hello <00:05.80>world
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals(1, parsed.lines.size)
        val line = parsed.lines[0]
        assertEquals("Hello world", line.text)
        assertEquals(2, line.words.size)
        assertEquals(5000L, line.words[0].timestampMs)
        assertEquals("Hello", line.words[0].word)
        assertEquals(5800L, line.words[1].timestampMs)
        assertEquals("world", line.words[1].word)
    }

    @Test
    fun findActiveLineIndex_matchesCurrentPlaybackPosition() {
        val lrc = """
            [00:05.00]Line 1
            [00:15.00]Line 2
            [00:30.00]Line 3
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)

        // Before first line
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 2000L))

        // On line 1
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 5000L))
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 10000L))

        // On line 2
        assertEquals(1, LrcParser.findActiveLineIndex(parsed.lines, 15000L))
        assertEquals(1, LrcParser.findActiveLineIndex(parsed.lines, 25000L))

        // On line 3
        assertEquals(2, LrcParser.findActiveLineIndex(parsed.lines, 30000L))
        assertEquals(2, LrcParser.findActiveLineIndex(parsed.lines, 90000L))
    }
}
