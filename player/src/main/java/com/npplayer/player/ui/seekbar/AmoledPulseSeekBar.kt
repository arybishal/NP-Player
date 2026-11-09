package com.npplayer.player.ui.seekbar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import kotlin.math.sin

@Composable
fun AmoledPulseSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean = false,
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

    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
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

        val barCount = 42
        val step = (endX - startX) / barCount

        // Draw rhythmically pulsing vertical mini bars
        for (i in 0 until barCount) {
            val barX = startX + i * step + step / 2f
            val isPassed = barX <= currentX

            val pulseFactor = if (isPlaying) {
                (sin(phase + i * 0.45f) * 0.5f + 0.5f)
            } else 0.3f

            val barHeight = (4.dp.toPx() + pulseFactor * 10.dp.toPx())
            val halfH = barHeight / 2f

            val color = if (isPassed) {
                NPYellow.copy(alpha = 0.85f + 0.15f * pulseFactor)
            } else {
                NPWhite.copy(alpha = 0.20f)
            }

            drawLine(
                color = color,
                start = Offset(barX, centerY - halfH),
                end = Offset(barX, centerY + halfH),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Diamond Playhead Thumb
        val diamondSize = if (isDragging) 9.dp.toPx() else 6.5.dp.toPx()
        val diamondPath = Path().apply {
            moveTo(currentX, centerY - diamondSize)
            lineTo(currentX + diamondSize, centerY)
            lineTo(currentX, centerY + diamondSize)
            lineTo(currentX - diamondSize, centerY)
            close()
        }

        // Diamond Outer Glow
        drawCircle(
            color = NPYellow.copy(alpha = 0.35f),
            radius = diamondSize * 1.5f,
            center = Offset(currentX, centerY)
        )

        // Draw yellow diamond
        drawPath(path = diamondPath, color = NPYellow)

        // Draw inner black diamond core
        val innerSize = diamondSize * 0.45f
        val innerDiamond = Path().apply {
            moveTo(currentX, centerY - innerSize)
            lineTo(currentX + innerSize, centerY)
            lineTo(currentX, centerY + innerSize)
            lineTo(currentX - innerSize, centerY)
            close()
        }
        drawPath(path = innerDiamond, color = NPBlack)
    }
}
