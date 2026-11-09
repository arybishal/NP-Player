package com.npplayer.core.data.repository

import com.npplayer.core.data.database.entity.SongEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uPlaylistHelperTest {

    @Test
    fun generateAndParseM3u_roundTrip() {
        val testSongs = listOf(
            SongEntity(
                id = 1,
                mediaStoreId = 101,
                uri = "content://media/external/audio/media/101",
                title = "Song One",
                artist = "Artist A",
                album = "Album 1",
                durationMs = 180000,
                sizeBytes = 5000000,
                dateAdded = 1000,
                dateModified = 1000,
                mimeType = "audio/mpeg",
                filePath = "/storage/emulated/0/Music/SongOne.mp3",
                folderPath = "/storage/emulated/0/Music",
                folderName = "Music"
            ),
            SongEntity(
                id = 2,
                mediaStoreId = 102,
                uri = "content://media/external/audio/media/102",
                title = "Song Two",
                artist = "Artist B",
                album = "Album 2",
                durationMs = 240000,
                sizeBytes = 6000000,
                dateAdded = 1000,
                dateModified = 1000,
                mimeType = "audio/flac",
                filePath = "/storage/emulated/0/Music/SongTwo.flac",
                folderPath = "/storage/emulated/0/Music",
                folderName = "Music"
            )
        )

        val m3uText = M3uPlaylistHelper.generateM3u("My Favorites", testSongs)
        assertTrue(m3uText.contains("#EXTM3U"))
        assertTrue(m3uText.contains("#PLAYLIST:My Favorites"))
        assertTrue(m3uText.contains("#EXTINF:180,Artist A - Song One"))
        assertTrue(m3uText.contains("/storage/emulated/0/Music/SongOne.mp3"))

        val parsed = M3uPlaylistHelper.parseM3u(m3uText)
        assertEquals(2, parsed.size)
        assertEquals(180L, parsed[0].durationSec)
        assertEquals("Artist A - Song One", parsed[0].title)
        assertEquals("/storage/emulated/0/Music/SongOne.mp3", parsed[0].pathOrUri)

        assertEquals(240L, parsed[1].durationSec)
        assertEquals("Artist B - Song Two", parsed[1].title)
        assertEquals("/storage/emulated/0/Music/SongTwo.flac", parsed[1].pathOrUri)
    }
}
