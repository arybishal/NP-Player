package com.npplayer.core.data.subtitles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SubtitleHasherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun isNetworkStream_identifiesRemoteUrls() {
        assertTrue(SubtitleHasher.isNetworkStream("http://example.com/video.mp4"))
        assertTrue(SubtitleHasher.isNetworkStream("https://stream.provider.com/live.m3u8"))
        assertTrue(SubtitleHasher.isNetworkStream("rtmp://192.168.1.50/live/ch1"))
        assertTrue(SubtitleHasher.isNetworkStream("rtsp://cam.local:554/stream1"))
    }

    @Test
    fun computeHash_skipsNetworkStreams() {
        assertNull(SubtitleHasher.computeHash("https://cdn.movies.com/sample.mp4"))
        assertNull(SubtitleHasher.computeHash("rtmp://live.stream/video"))
    }

    @Test
    fun computeHash_skipsFilesUnder128Kb() {
        val smallFile = tempFolder.newFile("small.mkv")
        smallFile.writeBytes(ByteArray(1024)) // 1 KB
        assertNull(SubtitleHasher.computeHash(smallFile))
    }

    @Test
    fun computeHash_producesValid16CharHexForLargeFile() {
        val testFile = tempFolder.newFile("test_movie.mkv")
        // Create 200 KB dummy binary file
        val size = 200 * 1024
        val data = ByteArray(size) { (it % 256).toByte() }
        testFile.writeBytes(data)

        val hash = SubtitleHasher.computeHash(testFile)
        assertNotNull(hash)
        assertEquals(16, hash?.length)
        assertTrue(hash!!.matches(Regex("^[0-9a-f]{16}$")))
    }

    @Test
    fun computeHash_deterministicOutput() {
        val file1 = tempFolder.newFile("movie1.mp4")
        val size = 150 * 1024
        val data = ByteArray(size) { 42 }
        file1.writeBytes(data)

        val hash1 = SubtitleHasher.computeHash(file1)
        val hash2 = SubtitleHasher.computeHash(file1)

        assertEquals(hash1, hash2)
    }
}
