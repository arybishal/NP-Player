package com.npplayer.core.ui.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {
    /**
     * Formats milliseconds into standard media time string:
     * - Under 1 hour: "mm:ss" (e.g. "04:15")
     * - 1 hour or more: "h:mm:ss" (e.g. "1:23:45")
     */
    fun formatDuration(millis: Long): String {
        if (millis <= 0L) return "00:00"
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, remainingMinutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", remainingMinutes, seconds)
        }
    }

    /**
     * Formats total milliseconds into "Xh Ym" or "Ym" (e.g., "11h 24m").
     */
    fun formatHoursAndMinutes(millis: Long): String {
        if (millis <= 0L) return "0m"
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60
        return if (hours > 0) {
            "${hours}h ${remainingMinutes}m"
        } else {
            "${remainingMinutes}m"
        }
    }

    /**
     * Formats bytes into human-readable file size (B, KB, MB, GB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[index])
    }
}
