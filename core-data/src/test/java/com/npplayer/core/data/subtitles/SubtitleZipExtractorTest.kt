package com.npplayer.core.data.subtitles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SubtitleZipExtractorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun extractSubtitleFromZip_extractsAndConvertsToUtf8() {
        val srtContent = """
            1
            00:00:01,000 --> 00:00:04,000
            Hello, welcome to NP Player!
        """.trimIndent()

        // Create in-memory ZIP archive
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("movie.srt"))
            zos.write(srtContent.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        val destination = tempFolder.newFile("extracted.en.srt")
        val result = SubtitleExtractor.extractSubtitleFromZip(baos.toByteArray(), destination)

        assertTrue(result.isSuccess)
        val extractedFile = result.getOrThrow()
        assertTrue(extractedFile.exists())
        assertEquals(srtContent, extractedFile.readText(StandardCharsets.UTF_8))
    }

    @Test
    fun detectCharsetAndDecode_stripsBom() {
        val raw = "Hello world"
        val bomUtf8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + raw.toByteArray(StandardCharsets.UTF_8)

        val decoded = SubtitleExtractor.detectCharsetAndDecode(bomUtf8)
        assertEquals("Hello world", decoded)
    }

    @Test
    fun extractSubtitleFromZip_failsOnEmptyArchiveWithoutSubtitles() {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("readme.txt"))
            zos.write("Some readme info".toByteArray())
            zos.closeEntry()
        }

        val destination = tempFolder.newFile("extracted.srt")
        val result = SubtitleExtractor.extractSubtitleFromZip(baos.toByteArray(), destination)
        assertTrue(result.isFailure)
    }
}
