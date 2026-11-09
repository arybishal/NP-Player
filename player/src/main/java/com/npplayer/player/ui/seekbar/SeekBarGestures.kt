package com.npplayer.player.ui.seekbar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Bulletproof unified gesture modifier for all seek bar styles.
 * Keyed on Unit to ensure pointer events are never dropped or aborted on state updates.
 * Correctly reports live preview position during scrubbing and fires seek once on release.
 */
fun Modifier.seekBarGestures(
    durationMs: Long,
    onScrubbing: (isScrubbing: Boolean, previewPositionMs: Long) -> Unit,
    onSeek: (targetPositionMs: Long) -> Unit,
    onDragProgressUpdate: ((Float) -> Unit)? = null,
    transformProgress: ((Float) -> Float)? = null
): Modifier = composed {
    val currentDuration = rememberUpdatedState(durationMs)
    val currentOnScrubbing = rememberUpdatedState(onScrubbing)
    val currentOnSeek = rememberUpdatedState(onSeek)
    val currentOnDragUpdate = rememberUpdatedState(onDragProgressUpdate)
    val currentTransform = rememberUpdatedState(transformProgress)

    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val width = size.width.toFloat()
            if (width <= 0) return@awaitEachGesture

            var fraction = (down.position.x / width).coerceIn(0f, 1f)
            val transformer = currentTransform.value
            if (transformer != null) {
                fraction = transformer(fraction)
            }

            currentOnDragUpdate.value?.invoke(fraction)
            val dur = currentDuration.value
            val previewMs = if (dur > 0) (fraction * dur).toLong().coerceIn(0L, dur) else 0L
            currentOnScrubbing.value(true, previewMs)

            var lastFraction = fraction

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (change.changedToUp()) {
                    change.consume()
                    break
                }
                if (change.positionChanged()) {
                    change.consume()
                    var dragFrac = (change.position.x / width).coerceIn(0f, 1f)
                    val trans = currentTransform.value
                    if (trans != null) {
                        dragFrac = trans(dragFrac)
                    }
                    lastFraction = dragFrac
                    currentOnDragUpdate.value?.invoke(dragFrac)
                    val d = currentDuration.value
                    val pMs = if (d > 0) (dragFrac * d).toLong().coerceIn(0L, d) else 0L
                    currentOnScrubbing.value(true, pMs)
                }
            }

            currentOnScrubbing.value(false, 0L)
            val finalDuration = currentDuration.value
            if (finalDuration > 0) {
                val targetMs = (lastFraction * finalDuration).toLong().coerceIn(0L, finalDuration)
                currentOnSeek.value(targetMs)
            }
        }
    }
}
