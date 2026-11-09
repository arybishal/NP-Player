package com.npplayer.player.ui.seekbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
fun NeonMinimalSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
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

    val thumbRadius by animateFloatAsState(
        targetValue = if (isDragging) 8f else 4.5f,
        animationSpec = tween(durationMillis = 150),
        label = "neonThumbRadius"
    )

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
        val startX = 0f
        val endX = size.width
        val currentX = (startX + displayProgress * (endX - startX)).coerceIn(startX, endX)

        val trackStrokePx = 2.dp.toPx()
        val thumbRadiusPx = thumbRadius.dp.toPx()

        // Background hairline
        drawLine(
            color = NPWhite.copy(alpha = 0.20f),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = trackStrokePx,
            cap = StrokeCap.Round
        )

        // Neon Yellow progress line
        if (currentX > startX) {
            drawLine(
                color = NPYellow,
                start = Offset(startX, centerY),
                end = Offset(currentX, centerY),
                strokeWidth = trackStrokePx,
                cap = StrokeCap.Round
            )
        }

        // Precision Pip Thumb
        drawCircle(
            color = NPYellow,
            radius = thumbRadiusPx,
            center = Offset(currentX, centerY)
        )
        // Inner black core for laser contrast
        drawCircle(
            color = NPBlack,
            radius = thumbRadiusPx * 0.45f,
            center = Offset(currentX, centerY)
        )
    }
}
