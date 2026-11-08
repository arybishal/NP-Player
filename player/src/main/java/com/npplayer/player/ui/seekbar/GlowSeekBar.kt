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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun GlowSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
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
            .height(40.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (size.width > 0 && durationMs > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
            }
            .pointerInput(durationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek((dragProgress * durationMs).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        val centerY = size.height / 2f
        val startX = 0f
        val endX = size.width
        val playedX = (endX * displayProgress).coerceIn(0f, endX)
        val trackHeight = if (isDragging) 5.dp.toPx() else 3.5.dp.toPx()

        // 1. Inactive track
        drawLine(
            color = NPWhite.copy(alpha = 0.20f),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )

        // 2. Played track with subtle gradient glow trail
        if (playedX > startX) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    NPYellow.copy(alpha = 0.70f),
                    NPYellow
                ),
                startX = startX,
                endX = playedX
            )
            drawLine(
                brush = gradientBrush,
                start = Offset(startX, centerY),
                end = Offset(playedX, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )
        }

        // 3. Radial Glow Halo around the Seek Thumb
        val glowCenter = Offset(playedX, centerY)
        drawCircle(
            color = NPYellow.copy(alpha = 0.15f),
            radius = 18.dp.toPx(),
            center = glowCenter
        )
        drawCircle(
            color = NPYellow.copy(alpha = 0.35f),
            radius = 13.dp.toPx(),
            center = glowCenter
        )

        // 4. Center Thumb
        drawCircle(
            color = NPBlack,
            radius = 7.5.dp.toPx(),
            center = glowCenter
        )
        drawCircle(
            color = NPYellow,
            radius = 6.dp.toPx(),
            center = glowCenter
        )
    }
}
