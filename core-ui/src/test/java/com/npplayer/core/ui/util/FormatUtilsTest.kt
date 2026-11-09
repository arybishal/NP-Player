package com.npplayer.core.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun testFormatDurationZeroAndNegative() {
        assertEquals("00:00", FormatUtils.formatDuration(0L))
        assertEquals("00:00", FormatUtils.formatDuration(-500L))
    }

    @Test
    fun testFormatDurationUnderOneMinute() {
        assertEquals("00:05", FormatUtils.formatDuration(5_000L))
        assertEquals("00:45", FormatUtils.formatDuration(45_000L))
    }

    @Test
    fun testFormatDurationUnderOneHour() {
        assertEquals("04:15", FormatUtils.formatDuration(255_000L)) // 4m 15s
        assertEquals("59:59", FormatUtils.formatDuration(3_599_000L))
    }

    @Test
    fun testFormatDurationOneHourOrMore() {
        assertEquals("1:00:00", FormatUtils.formatDuration(3_600_000L)) // 1h
        assertEquals("2:15:30", FormatUtils.formatDuration(8_130_000L)) // 2h 15m 30s
    }

    @Test
    fun testFormatFileSizeZeroAndNegative() {
        assertEquals("0 B", FormatUtils.formatFileSize(0L))
        assertEquals("0 B", FormatUtils.formatFileSize(-100L))
    }

    @Test
    fun testFormatFileSizeRanges() {
        assertEquals("500.0 B", FormatUtils.formatFileSize(500L))
        assertEquals("1.0 KB", FormatUtils.formatFileSize(1024L))
        assertEquals("11.5 MB", FormatUtils.formatFileSize(12_058_624L))
        assertEquals("1.5 GB", FormatUtils.formatFileSize(1_610_612_736L))
    }
}
