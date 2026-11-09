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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import kotlin.math.sin

@Composable
fun WaveSeekBar(
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

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val wavePhase by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_phase"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
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
        val thumbX = size.width * displayProgress
        val barCount = 50
        val barWidth = size.width / barCount
        val maxWaveAmp = 8.dp.toPx()

        for (i in 0 until barCount) {
            val barCenterX = i * barWidth + barWidth / 2f
            val isPlayed = barCenterX <= thumbX

            // Sine wave height calculation
            val waveHeight = if (isPlayed && isPlaying) {
                val phase = wavePhase + (i * 0.35f)
                (sin(phase) * maxWaveAmp * 0.8f).toFloat() + 4.dp.toPx()
            } else if (isPlayed) {
                4.5.dp.toPx()
            } else {
                2.5.dp.toPx()
            }

            val barColor = if (isPlayed) NPYellow else NPWhite.copy(alpha = 0.20f)

            drawLine(
                color = barColor,
                start = Offset(barCenterX, centerY - waveHeight),
                end = Offset(barCenterX, centerY + waveHeight),
                strokeWidth = barWidth * 0.65f,
                cap = StrokeCap.Round
            )
        }

        // Thumb Head
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
