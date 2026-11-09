package com.npplayer.core.data.subtitles

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

object SubtitleExtractor {

    private val VALID_EXTENSIONS = listOf(".srt", ".vtt", ".ass", ".sub")

    /**
     * Extracts the primary subtitle file from a downloaded ZIP archive,
     * detects and converts encoding to clean UTF-8, and saves to [destinationFile].
     */
    fun extractSubtitleFromZip(
        zipBytes: ByteArray,
        destinationFile: File
    ): Result<File> {
        return try {
            val candidates = mutableListOf<Pair<String, ByteArray>>()

            ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()
                    if (!entry.isDirectory && VALID_EXTENSIONS.any { name.endsWith(it) }) {
                        val bytes = zis.readBytes()
                        if (bytes.isNotEmpty()) {
                            candidates.add(entry.name to bytes)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (candidates.isEmpty()) {
                // If it wasn't a valid ZIP or had no zip entries, check if zipBytes itself is a raw subtitle
                val rawText = detectCharsetAndDecode(zipBytes)
                if (rawText.contains("-->") || rawText.contains("[Script Info]")) {
                    destinationFile.parentFile?.mkdirs()
                    destinationFile.writeText(rawText, StandardCharsets.UTF_8)
                    return Result.success(destinationFile)
                }
                return Result.failure(IllegalStateException("No valid subtitle (.srt, .vtt, .ass) found in archive"))
            }

            // Pick the best candidate: prefer .srt files, then largest size
            val best = candidates.sortedWith(
                compareByDescending<Pair<String, ByteArray>> { it.first.lowercase().endsWith(".srt") }
                    .thenByDescending { it.second.size }
            ).first()

            val decodedContent = detectCharsetAndDecode(best.second)

            destinationFile.parentFile?.mkdirs()
            FileOutputStream(destinationFile).use { fos ->
                fos.write(decodedContent.toByteArray(StandardCharsets.UTF_8))
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detects charset (UTF-8, UTF-16, windows-1252, ISO-8859-1) and decodes cleanly to string.
     */
    fun detectCharsetAndDecode(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            // UTF-8 BOM
            return String(bytes, 3, bytes.size - 3, StandardCharsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            // UTF-16 LE BOM
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            // UTF-16 BE BOM
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16BE)
        }

        // Try strict UTF-8 decoding
        val utf8Decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        return try {
            utf8Decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (_: Exception) {
            // Fallback to windows-1252 / ISO-8859-1
            try {
                String(bytes, Charset.forName("windows-1252"))
            } catch (_: Exception) {
                String(bytes, StandardCharsets.ISO_8859_1)
            }
        }
    }
}
