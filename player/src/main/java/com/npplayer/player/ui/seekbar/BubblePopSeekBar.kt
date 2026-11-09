package com.npplayer.player.ui.seekbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun BubblePopSeekBar(
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

    val bubbleRadius by animateFloatAsState(
        targetValue = if (isDragging) 10f else 6f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "bubblePopRadius"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
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

        val trackStrokePx = 3.5.dp.toPx()

        // Background track
        drawLine(
            color = NPWhite.copy(alpha = 0.20f),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = trackStrokePx,
            cap = StrokeCap.Round
        )

        // Periodic milestone bubble dots (at 10%, 20%, ... 90%)
        val milestoneSteps = 10
        for (i in 1 until milestoneSteps) {
            val stepFraction = i.toFloat() / milestoneSteps
            val dotX = startX + stepFraction * (endX - startX)
            val isPassed = dotX <= currentX
            drawCircle(
                color = if (isPassed) NPYellow else NPWhite.copy(alpha = 0.40f),
                radius = 2.dp.toPx(),
                center = Offset(dotX, centerY)
            )
        }

        // Active Track
        if (currentX > startX) {
            drawLine(
                color = NPYellow,
                start = Offset(startX, centerY),
                end = Offset(currentX, centerY),
                strokeWidth = trackStrokePx,
                cap = StrokeCap.Round
            )
        }

        val thumbRadiusPx = bubbleRadius.dp.toPx()

        // Outer pulsing bubble halo if dragging
        if (isDragging) {
            drawCircle(
                color = NPYellow.copy(alpha = 0.25f),
                radius = thumbRadiusPx * 1.8f,
                center = Offset(currentX, centerY)
            )
        }

        // Outer bubble ring
        drawCircle(
            color = NPYellow,
            radius = thumbRadiusPx,
            center = Offset(currentX, centerY)
        )

        // Inner contrasting core
        drawCircle(
            color = NPBlack,
            radius = thumbRadiusPx * 0.55f,
            center = Offset(currentX, centerY)
        )

        // Center dot
        drawCircle(
            color = NPYellow,
            radius = thumbRadiusPx * 0.25f,
            center = Offset(currentX, centerY)
        )
    }
}
