package com.npplayer.player.ui.seekbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import kotlin.math.abs

@Composable
fun ChaptersSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    chapterCount: Int = 5,
    onScrubbing: (Boolean, Long) -> Unit = { _, _ -> },
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayProgress = if (isDragging) dragProgress else actualProgress

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .seekBarGestures(
                durationMs = durationMs,
                onScrubbing = { scrubbing, previewMs ->
                    isDragging = scrubbing
                    onScrubbing(scrubbing, previewMs)
                },
                onSeek = onSeek,
                onDragProgressUpdate = { dragProgress = it },
                transformProgress = { raw -> snapToNearestChapter(raw, chapterCount, threshold = 0.02f) }
            )
    ) {
        val centerY = size.height / 2f
        val gapPx = 3.dp.toPx()
        val totalGaps = (chapterCount - 1) * gapPx
        val totalTrackWidth = size.width - totalGaps
        val segmentWidth = totalTrackWidth / chapterCount

        val currentChapterIndex = (displayProgress * chapterCount).toInt().coerceIn(0, chapterCount - 1)
        val thumbX = size.width * displayProgress

        for (i in 0 until chapterCount) {
            val segStart = i * (segmentWidth + gapPx)
            val segEnd = segStart + segmentWidth
            val isCurrentChapter = i == currentChapterIndex
            val trackHeight = if (isCurrentChapter) 6.5.dp.toPx() else 3.5.dp.toPx()

            // Inactive segment
            drawLine(
                color = NPWhite.copy(alpha = 0.20f),
                start = Offset(segStart, centerY),
                end = Offset(segEnd, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Active played segment
            if (thumbX >= segStart) {
                val fillEnd = thumbX.coerceAtMost(segEnd)
                drawLine(
                    color = NPYellow,
                    start = Offset(segStart, centerY),
                    end = Offset(fillEnd, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
            }
        }

        // Thumb handle
        drawCircle(
            color = NPBlack,
            radius = 8.dp.toPx(),
            center = Offset(thumbX, centerY)
        )
        drawCircle(
            color = NPYellow,
            radius = 6.5.dp.toPx(),
            center = Offset(thumbX, centerY)
        )
    }
}

private fun snapToNearestChapter(fraction: Float, chapterCount: Int, threshold: Float = 0.035f): Float {
    val step = 1f / chapterCount
    for (i in 0..chapterCount) {
        val chapterBoundary = i * step
        if (abs(fraction - chapterBoundary) <= threshold) {
            return chapterBoundary
        }
    }
    return fraction
}
