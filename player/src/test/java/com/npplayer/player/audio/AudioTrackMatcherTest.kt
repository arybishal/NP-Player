package com.npplayer.player.audio

import com.npplayer.core.data.database.entity.VideoAudioPreferenceEntity
import com.npplayer.player.engine.MediaTrackItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AudioTrackMatcherTest {

    private val trackEnAac2 = MediaTrackItem(
        id = "0-0",
        name = "English (AAC 2.0)",
        language = "en",
        isSelected = true,
        groupIndex = 0,
        trackIndex = 0,
        codec = "AAC",
        channels = "2.0",
        channelCount = 2
    )

    private val trackHiAc3_51 = MediaTrackItem(
        id = "0-1",
        name = "Hindi (AC3 5.1)",
        language = "hi",
        isSelected = false,
        groupIndex = 0,
        trackIndex = 1,
        codec = "AC3",
        channels = "5.1",
        channelCount = 6
    )

    private val trackTaEac3_51 = MediaTrackItem(
        id = "0-2",
        name = "Tamil (E-AC3 5.1)",
        language = "ta",
        isSelected = false,
        groupIndex = 0,
        trackIndex = 2,
        codec = "E-AC3",
        channels = "5.1",
        channelCount = 6
    )

    private val allTracks = listOf(trackEnAac2, trackHiAc3_51, trackTaEac3_51)

    @Test
    fun `exact match by language, codec and channelCount wins`() {
        val pref = VideoAudioPreferenceEntity(
            videoId = 123L,
            videoPath = "/movies/film.mp4",
            folderPath = "/movies",
            languageCode = "hi",
            trackLabel = "",
            codec = "AC3",
            channelCount = 6,
            trackIndex = 1
        )

        val match = AudioTrackMatcher.findBestMatch(allTracks, pref, null, emptyList())
        assertNotNull(match)
        assertEquals("hi", match?.language)
        assertEquals("AC3", match?.codec)
        assertEquals(6, match?.channelCount)
    }

    @Test
    fun `fallback to language only when codec differs`() {
        val pref = VideoAudioPreferenceEntity(
            videoId = 123L,
            videoPath = "/movies/film.mp4",
            folderPath = "/movies",
            languageCode = "ta",
            trackLabel = "",
            codec = "DTS", // file only has E-AC3 for Tamil
            channelCount = 6,
            trackIndex = 2
        )

        val match = AudioTrackMatcher.findBestMatch(allTracks, pref, null, emptyList())
        assertNotNull(match)
        assertEquals("ta", match?.language)
        assertEquals("E-AC3", match?.codec)
    }

    @Test
    fun `fallback to track index when language code changed or is und`() {
        val pref = VideoAudioPreferenceEntity(
            videoId = 123L,
            videoPath = "/movies/film.mp4",
            folderPath = "/movies",
            languageCode = "fr", // French not in file
            trackLabel = "",
            codec = "MP3",
            channelCount = 2,
            trackIndex = 1 // points to trackHiAc3_51
        )

        val match = AudioTrackMatcher.findBestMatch(allTracks, pref, null, emptyList())
        assertNotNull(match)
        assertEquals(trackHiAc3_51.id, match?.id)
    }

    @Test
    fun `folder series preference applies when video has no saved preference`() {
        val folderPref = VideoAudioPreferenceEntity(
            videoId = 999L,
            videoPath = "/series/ep01.mp4",
            folderPath = "/series",
            languageCode = "ta",
            trackLabel = "",
            codec = "E-AC3",
            channelCount = 6,
            trackIndex = 2
        )

        val match = AudioTrackMatcher.findBestMatch(allTracks, null, folderPref, emptyList())
        assertNotNull(match)
        assertEquals("ta", match?.language)
    }

    @Test
    fun `preferred audio languages list used as fallback in priority order`() {
        val match = AudioTrackMatcher.findBestMatch(
            allTracks,
            null,
            null,
            listOf("fr", "hi", "en") // French not available, Hindi is available
        )
        assertNotNull(match)
        assertEquals("hi", match?.language)
    }

    @Test
    fun `empty tracks returns null safely`() {
        val match = AudioTrackMatcher.findBestMatch(emptyList(), null, null, listOf("en"))
        assertNull(match)
    }
}
