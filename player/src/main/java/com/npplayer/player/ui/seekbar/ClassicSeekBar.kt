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

@Composable
fun ClassicSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long = 0L,
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

    val bufferedProgress = if (durationMs > 0) {
        (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

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
                onDragProgressUpdate = { dragProgress = it }
            )
    ) {
        val centerY = size.height / 2f
        val trackHeight = if (isDragging) 6.dp.toPx() else 3.5.dp.toPx()
        val thumbRadius = if (isDragging) 9.dp.toPx() else 6.5.dp.toPx()

        val startX = 0f
        val endX = size.width
        val playedX = (endX * displayProgress).coerceIn(0f, endX)
        val bufferedX = (endX * bufferedProgress).coerceIn(0f, endX)

        // 1. Inactive Background Track
        drawLine(
            color = NPWhite.copy(alpha = 0.20f),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )

        // 2. Buffered Track
        if (bufferedX > startX) {
            drawLine(
                color = NPWhite.copy(alpha = 0.40f),
                start = Offset(startX, centerY),
                end = Offset(bufferedX, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )
        }

        // 3. Played Progress Track (Strict Yellow)
        if (playedX > startX) {
            drawLine(
                color = NPYellow,
                start = Offset(startX, centerY),
                end = Offset(playedX, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )
        }

        // 4. Thumb Handle (Strict Yellow with black rim)
        drawCircle(
            color = NPBlack,
            radius = thumbRadius + 1.5.dp.toPx(),
            center = Offset(playedX, centerY)
        )
        drawCircle(
            color = NPYellow,
            radius = thumbRadius,
            center = Offset(playedX, centerY)
        )
    }
}
