package com.npplayer.player.ui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

fun Modifier.playerVideoGestures(
    enabled: Boolean = true,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onLongPress: () -> Unit,
    onLongPressRelease: () -> Unit,
    onTwoFingerDoubleTap: () -> Unit,
    onZoomAndPan: (zoomDelta: Float, panDelta: Offset) -> Unit,
    onBrightnessDrag: (deltaPercent: Float) -> Unit,
    onVolumeDrag: (deltaPercent: Float) -> Unit,
    onDragStart: (isBrightness: Boolean) -> Unit,
    onDragEnd: () -> Unit
): Modifier = if (!enabled) this else pointerInput(Unit) {
    var lastTwoFingerTapTime = 0L
    var lastSingleTapTime = 0L
    var lastSingleTapOffset = Offset.Zero

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downTime = System.currentTimeMillis()
        val startPos = down.position
        var isMultiTouch = false
        var isVerticalDrag = false
        var isBrightness = false
        var isVolume = false
        var isLongPressTriggered = false
        var prevDistance = -1f
        var prevCentroid = Offset.Zero
        var hasMovedSignificantly = false

        while (true) {
            val event = awaitPointerEvent()
            val activePointers = event.changes.filter { it.pressed }

            if (activePointers.isEmpty()) {
                break
            }

            if (activePointers.size >= 2) {
                // Multi-touch: Zoom and Pan
                isMultiTouch = true
                val p1 = activePointers[0].position
                val p2 = activePointers[1].position
                val currentDistance = (p1 - p2).getDistance()
                val currentCentroid = (p1 + p2) / 2f

                if (prevDistance > 0f) {
                    val zoomDelta = if (prevDistance > 0f) currentDistance / prevDistance else 1.0f
                    val panDelta = currentCentroid - prevCentroid
                    if (abs(currentDistance - prevDistance) > 3f || panDelta.getDistance() > 3f) {
                        hasMovedSignificantly = true
                    }
                    onZoomAndPan(zoomDelta, panDelta)
                }
                prevDistance = currentDistance
                prevCentroid = currentCentroid
                activePointers.forEach { it.consume() }
            } else if (activePointers.size == 1 && !isMultiTouch) {
                val current = activePointers[0]
                val diff = current.position - startPos
                val elapsed = System.currentTimeMillis() - downTime

                if (!isVerticalDrag && !hasMovedSignificantly) {
                    if (elapsed >= 500L && diff.getDistance() < 24f && !isLongPressTriggered) {
                        isLongPressTriggered = true
                        onLongPress()
                    }
                }

                if (!isLongPressTriggered) {
                    if (!isVerticalDrag && abs(diff.y) > 24f && abs(diff.y) > abs(diff.x) * 1.3f) {
                        isVerticalDrag = true
                        hasMovedSignificantly = true
                        isBrightness = startPos.x < size.width * 0.45f
                        isVolume = startPos.x > size.width * 0.55f
                        if (isBrightness || isVolume) {
                            onDragStart(isBrightness)
                        }
                    }

                    if (isVerticalDrag) {
                        val deltaY = current.position.y - current.previousPosition.y
                        val percentDelta = -deltaY / 4f
                        if (isBrightness) {
                            onBrightnessDrag(percentDelta)
                            current.consume()
                        } else if (isVolume) {
                            onVolumeDrag(percentDelta)
                            current.consume()
                        }
                    }
                } else {
                    current.consume()
                }
            }
        }

        // Gesture finished
        if (isLongPressTriggered) {
            onLongPressRelease()
        } else if (isMultiTouch) {
            val totalTime = System.currentTimeMillis() - downTime
            if (!hasMovedSignificantly && totalTime < 350L) {
                // Two-finger tap
                val now = System.currentTimeMillis()
                if (now - lastTwoFingerTapTime < 400L) {
                    onTwoFingerDoubleTap()
                    lastTwoFingerTapTime = 0L
                } else {
                    lastTwoFingerTapTime = now
                }
            }
        } else if (isVerticalDrag) {
            onDragEnd()
        } else {
            // Single finger tap or double tap
            val totalTime = System.currentTimeMillis() - downTime
            val totalDist = (startPos - down.position).getDistance()
            if (totalTime < 300L && totalDist < 24f) {
                val now = System.currentTimeMillis()
                if (now - lastSingleTapTime < 350L && (startPos - lastSingleTapOffset).getDistance() < 80f) {
                    onDoubleTap(startPos)
                    lastSingleTapTime = 0L
                } else {
                    lastSingleTapTime = now
                    lastSingleTapOffset = startPos
                    onTap()
                }
            }
        }
    }
}
