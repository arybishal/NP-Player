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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun RetroBlocksSeekBar(
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
        val totalWidth = size.width

        val blockCount = 36
        val gapPx = 2.5.dp.toPx()
        val totalGapsWidth = gapPx * (blockCount - 1)
        val blockWidth = (totalWidth - totalGapsWidth) / blockCount
        val blockHeight = 8.dp.toPx()
        val activeBlockIndex = (displayProgress * blockCount).toInt().coerceIn(0, blockCount)

        for (i in 0 until blockCount) {
            val left = i * (blockWidth + gapPx)
            val isFilled = i < activeBlockIndex
            val isCurrentThumbBlock = i == activeBlockIndex

            val blockH = if (isCurrentThumbBlock && isDragging) blockHeight * 1.6f else blockHeight
            val top = centerY - blockH / 2f

            val color = when {
                isCurrentThumbBlock -> NPYellow
                isFilled -> NPYellow.copy(alpha = 0.95f)
                else -> NPWhite.copy(alpha = 0.20f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(blockWidth, blockH),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )

            if (isCurrentThumbBlock) {
                // Glow marker for the head block
                drawRoundRect(
                    color = NPBlack.copy(alpha = 0.40f),
                    topLeft = Offset(left + blockWidth * 0.25f, top + blockH * 0.25f),
                    size = Size(blockWidth * 0.5f, blockH * 0.5f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }
    }
}
