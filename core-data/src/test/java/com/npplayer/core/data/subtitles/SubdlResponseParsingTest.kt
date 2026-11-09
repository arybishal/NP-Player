package com.npplayer.core.data.subtitles

import com.npplayer.core.data.subtitles.subdl.SubdlApiResponse
import com.npplayer.core.data.subtitles.subdl.SubdlProvider
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubdlResponseParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun parseSubdlSuccessResponse() {
        val sampleJson = """
            {
                "status": true,
                "subtitles": [
                    {
                        "release_name": "Inception.2010.1080p.BluRay.x264",
                        "name": "Inception",
                        "lang": "English",
                        "language": "en",
                        "author": "golden_subs",
                        "url": "/subtitle/48291-inception.zip",
                        "downloads": 1420,
                        "hi": false
                    },
                    {
                        "release_name": "Inception.2010.720p.WEB-DL",
                        "name": "Inception",
                        "lang": "Spanish",
                        "language": "es",
                        "author": "cinefilo",
                        "url": "/subtitle/48292-inception-es.zip",
                        "downloads": 310,
                        "hi": true
                    }
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString<SubdlApiResponse>(sampleJson)
        assertTrue(response.status)
        assertNotNull(response.subtitles)
        assertEquals(2, response.subtitles?.size)

        val first = response.subtitles!![0]
        assertEquals("Inception.2010.1080p.BluRay.x264", first.releaseName)
        assertEquals("en", first.language)
        assertEquals(1420, first.downloads)
        assertEquals(false, first.hi)
    }

    @Test
    fun parseSubdlErrorResponse() {
        val errorJson = """
            {
                "status": false,
                "error": "Daily limit reached for this IP"
            }
        """.trimIndent()

        val response = json.decodeFromString<SubdlApiResponse>(errorJson)
        assertFalse(response.status)
        assertEquals("Daily limit reached for this IP", response.error)
    }

    @Test
    fun downloadUrlValidation_enforcesDlSubdlHost() {
        val provider = SubdlProvider()

        // Valid relative URL
        val relative = provider.buildAndValidateDownloadUrl("/subtitle/123-test.zip")
        assertEquals("https://dl.subdl.com/subtitle/123-test.zip", relative)

        // Valid absolute URL with allowed host
        val validAbsolute = provider.buildAndValidateDownloadUrl("https://dl.subdl.com/subtitle/123-test.zip")
        assertEquals("https://dl.subdl.com/subtitle/123-test.zip", validAbsolute)

        // Untrusted/foreign host should be rejected
        val untrusted = provider.buildAndValidateDownloadUrl("https://evil-server.com/malware.zip")
        assertNull(untrusted)
    }
}
