package com.npplayer.player.ui.seekbar

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun GradientFlameSeekBar(
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

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        label = "pillScale"
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
        val trackHeight = 4.dp.toPx()

        // Background track
        drawRoundRect(
            color = NPWhite.copy(alpha = 0.22f),
            topLeft = Offset(startX, centerY - trackHeight / 2f),
            size = Size(endX - startX, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
        )

        // Gradient flame active track
        if (currentX > startX) {
            val flameBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFE5A100), // Rich amber gold
                    NPYellow,          // Pure brilliant Yellow
                    Color(0xFFFFEA79)  // Hot incandescent tip
                ),
                startX = startX,
                endX = currentX
            )

            drawRoundRect(
                brush = flameBrush,
                topLeft = Offset(startX, centerY - trackHeight / 2f),
                size = Size(currentX - startX, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Trailing halo behind thumb
            drawCircle(
                color = NPYellow.copy(alpha = 0.35f),
                radius = 12.dp.toPx() * thumbScale,
                center = Offset(currentX, centerY)
            )
        }

        // Sleek Capsule Pill Thumb
        val pillWidth = 5.dp.toPx() * thumbScale
        val pillHeight = 18.dp.toPx() * thumbScale
        val pillLeft = (currentX - pillWidth / 2f).coerceIn(0f, endX - pillWidth)
        val pillTop = centerY - pillHeight / 2f

        drawRoundRect(
            color = NPYellow,
            topLeft = Offset(pillLeft, pillTop),
            size = Size(pillWidth, pillHeight),
            cornerRadius = CornerRadius(pillWidth / 2f, pillWidth / 2f)
        )

        // Subtle inner accent line
        drawRoundRect(
            color = NPBlack,
            topLeft = Offset(pillLeft + pillWidth * 0.3f, pillTop + pillHeight * 0.25f),
            size = Size(pillWidth * 0.4f, pillHeight * 0.5f),
            cornerRadius = CornerRadius(pillWidth * 0.2f, pillWidth * 0.2f)
        )
    }
}
