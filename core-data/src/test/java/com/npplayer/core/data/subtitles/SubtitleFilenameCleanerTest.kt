package com.npplayer.core.data.subtitles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubtitleFilenameCleanerTest {

    @Test
    fun clean_tvEpisodeWithTags() {
        val raw = "Dead-End.Job.S01E08.1080p.WEB-DL.x264-GECKOS.mkv"
        val info = SubtitleFilenameCleaner.clean(raw)

        assertEquals("Dead End Job", info.cleanTitle)
        assertEquals(1, info.season)
        assertEquals(8, info.episode)
        assertEquals("tv", info.type)
        assertEquals("Dead End Job S01E08", info.suggestedQuery)
    }

    @Test
    fun clean_movieWithYearAndQuality() {
        val raw = "Inception.2010.1080p.BluRay.x264.YIFY.mp4"
        val info = SubtitleFilenameCleaner.clean(raw)

        assertEquals("Inception", info.cleanTitle)
        assertEquals(2010, info.year)
        assertNull(info.season)
        assertNull(info.episode)
        assertEquals("movie", info.type)
        assertEquals("Inception 2010", info.suggestedQuery)
    }

    @Test
    fun clean_bracketsAndReleaseGroup() {
        val raw = "[HorribleSubs] Spy x Family - 01 [1080p].mkv"
        val info = SubtitleFilenameCleaner.clean(raw)

        assertEquals("Spy x Family", info.cleanTitle)
    }

    @Test
    fun clean_dotsAndUnderscores() {
        val raw = "The_Grand_Budapest_Hotel_2014_720p.mkv"
        val info = SubtitleFilenameCleaner.clean(raw)

        assertEquals("The Grand Budapest Hotel", info.cleanTitle)
        assertEquals(2014, info.year)
        assertEquals("movie", info.type)
    }

    @Test
    fun clean_shortNameWithoutTags() {
        val raw = "sample_video.mp4"
        val info = SubtitleFilenameCleaner.clean(raw)

        assertEquals("sample video", info.cleanTitle)
    }
}
