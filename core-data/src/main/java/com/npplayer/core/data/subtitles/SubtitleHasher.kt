package com.npplayer.core.data.subtitles

import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

object SubtitleHasher {

    private const val HASH_CHUNK_SIZE = 65536L // 64 KB

    /**
     * Computes the 64-bit MovieHash (OpenSubtitles hash) for a local media file.
     * Skips network streams (e.g., http/https/rtmp) or files smaller than 128 KB.
     *
     * @param path File path or stream URL
     * @return 16-character lowercase hex hash string, or null if unsupported/failed
     */
    fun computeHash(path: String): String? {
        if (isNetworkStream(path)) {
            return null
        }
        val file = File(path)
        if (!file.exists() || !file.isFile || file.length() < HASH_CHUNK_SIZE * 2) {
            return null
        }
        return computeHash(file)
    }

    fun computeHash(file: File): String? {
        val size = file.length()
        if (size < HASH_CHUNK_SIZE * 2) {
            return null
        }
        return try {
            FileInputStream(file).use { fis ->
                fis.channel.use { channel ->
                    computeHash(channel, size)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun computeHash(channel: FileChannel, size: Long): String {
        var hash = size
        val buffer = ByteBuffer.allocate(HASH_CHUNK_SIZE.toInt()).order(ByteOrder.LITTLE_ENDIAN)

        // Read first 64 KB
        channel.position(0)
        channel.read(buffer)
        buffer.flip()
        while (buffer.hasRemaining()) {
            hash += buffer.long
        }

        // Read last 64 KB
        buffer.clear()
        val lastPos = (size - HASH_CHUNK_SIZE).coerceAtLeast(0L)
        channel.position(lastPos)
        channel.read(buffer)
        buffer.flip()
        while (buffer.hasRemaining()) {
            hash += buffer.long
        }

        return String.format("%016x", hash)
    }

    fun isNetworkStream(path: String): Boolean {
        val lower = path.lowercase()
        return lower.startsWith("http://") ||
                lower.startsWith("https://") ||
                lower.startsWith("rtsp://") ||
                lower.startsWith("rtmp://") ||
                lower.startsWith("mms://")
    }
}
