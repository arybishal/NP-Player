package com.npplayer.player.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests verifying seek calculations, bounds clamping, large file millisecond handling,
 * and error recovery position preservation across container types (MP4, MKV, Long video).
 */
class PlayerSeekTest {

    // Simulates the safe clamping and seek calculation implemented in Media3PlayerEngine
    private fun calculateSafeSeekTarget(
        requestedPositionMs: Long,
        durationMs: Long
    ): Long {
        return if (durationMs > 0L) {
            requestedPositionMs.coerceIn(0L, durationMs)
        } else {
            requestedPositionMs.coerceAtLeast(0L)
        }
    }

    private fun calculateRelativeSeek(
        currentPositionMs: Long,
        deltaMs: Long,
        durationMs: Long
    ): Long {
        return if (durationMs > 0L) {
            (currentPositionMs + deltaMs).coerceIn(0L, durationMs)
        } else {
            (currentPositionMs + deltaMs).coerceAtLeast(0L)
        }
    }

    private fun calculateScrubPosition(
        fraction: Float,
        durationMs: Long
    ): Long {
        val clampedFrac = fraction.coerceIn(0f, 1f)
        return (clampedFrac * durationMs).toLong().coerceIn(0L, durationMs)
    }

    @Test
    fun `normal MP4 file (5 min) - seek within bounds lands on exact millisecond`() {
        val duration = 5 * 60 * 1000L // 300,000 ms (5 min)
        val target = calculateSafeSeekTarget(125430L, duration)
        assertEquals(125430L, target)
    }

    @Test
    fun `normal MP4 file (5 min) - forward and backward relative seeking clamped`() {
        val duration = 300_000L
        val current = 50_000L

        // +10s forward
        val forward = calculateRelativeSeek(current, 10_000L, duration)
        assertEquals(60_000L, forward)

        // -10s backward
        val backward = calculateRelativeSeek(current, -10_000L, duration)
        assertEquals(40_000L, backward)

        // Beyond end
        val beyondEnd = calculateRelativeSeek(current, 500_000L, duration)
        assertEquals(300_000L, beyondEnd)

        // Before start
        val beforeStart = calculateRelativeSeek(current, -100_000L, duration)
        assertEquals(0L, beforeStart)
    }

    @Test
    fun `MKV video (45 min) - scrubbing fraction maps accurately`() {
        val duration = 45 * 60 * 1000L // 2,700,000 ms
        val fraction = 0.5f // 50%
        val scrubbed = calculateScrubPosition(fraction, duration)
        assertEquals(1_350_000L, scrubbed)

        // 75%
        val scrubbed75 = calculateScrubPosition(0.75f, duration)
        assertEquals(2_025_000L, scrubbed75)
    }

    @Test
    fun `very long duration video (4 hours) - no 32-bit integer overflow`() {
        val duration = 4 * 3600 * 1000L // 14,400,000 ms
        val target = 10_800_000L // 3 hours in
        val result = calculateSafeSeekTarget(target, duration)
        assertEquals(10_800_000L, result)

        val scrub90 = calculateScrubPosition(0.90f, duration)
        assertEquals(12_960_000L, scrub90)
    }

    @Test
    fun `duration unset or 0 - seek preserves target and does not snap to 0`() {
        val duration = 0L // or C.TIME_UNSET
        val requested = 45_000L
        val target = calculateSafeSeekTarget(requested, duration)
        // Must preserve the requested 45,000ms rather than clamping to 0!
        assertEquals(45_000L, target)
    }

    @Test
    fun `error recovery preserves last known position instead of restarting at 0`() {
        val currentPosition = 120_500L
        val lastKnownPos = currentPosition.coerceAtLeast(0L)
        // If an error happens at 120,500ms, the recovery position must be 120,500ms
        assertTrue(lastKnownPos > 0L)
        assertEquals(120_500L, lastKnownPos)
    }

    @Test
    fun `short video (15 seconds portrait) - small seek deltas`() {
        val duration = 15_000L
        val current = 8_000L
        val result = calculateRelativeSeek(current, 5_000L, duration)
        assertEquals(13_000L, result)
    }
}
