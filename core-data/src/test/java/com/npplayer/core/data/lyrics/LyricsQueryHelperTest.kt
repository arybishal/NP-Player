package com.npplayer.core.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsQueryHelperTest {

    @Test
    fun cleanTitle_removesBracketsAndVideoTags() {
        assertEquals("Blinding Lights", LyricsQueryHelper.cleanTitle("01. Blinding Lights (Official Video)"))
        assertEquals("Starboy", LyricsQueryHelper.cleanTitle("02 - Starboy [Official Music Video]"))
        assertEquals("Shape of You", LyricsQueryHelper.cleanTitle("Shape of You (Lyrics)"))
        assertEquals("Levitating", LyricsQueryHelper.cleanTitle("Levitating (feat. DaBaby)"))
        assertEquals("Bohemian Rhapsody", LyricsQueryHelper.cleanTitle("Bohemian Rhapsody [Remastered 2011]"))
    }

    @Test
    fun normalizeArtist_handlesMultipleArtistsAndFeatures() {
        assertEquals("The Weeknd", LyricsQueryHelper.normalizeArtist("The Weeknd feat. Daft Punk"))
        assertEquals("Calvin Harris", LyricsQueryHelper.normalizeArtist("Calvin Harris, Dua Lipa"))
        assertEquals("Drake", LyricsQueryHelper.normalizeArtist("Drake / Future"))
        assertEquals("Ed Sheeran", LyricsQueryHelper.normalizeArtist("Ed Sheeran & Justin Bieber"))
        assertEquals("", LyricsQueryHelper.normalizeArtist("<unknown>"))
    }
}
