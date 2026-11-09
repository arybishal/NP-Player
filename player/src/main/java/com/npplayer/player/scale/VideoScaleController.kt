package com.npplayer.player.scale

import androidx.compose.ui.geometry.Offset
import androidx.media3.ui.AspectRatioFrameLayout
import com.npplayer.core.data.preferences.AspectRatioMode

data class VideoScaleInput(
    val videoWidth: Int,
    val videoHeight: Int,
    val pixelWidthHeightRatio: Float = 1.0f,
    val rotationDegrees: Int = 0,
    val viewWidth: Float,
    val viewHeight: Float,
    val mode: AspectRatioMode = AspectRatioMode.FIT,
    val customRatio: Float? = null,
    val userZoom: Float = 1.0f,
    val userPan: Offset = Offset.Zero
)

data class VideoScaleResult(
    val scaleX: Float,
    val scaleY: Float,
    val translationX: Float,
    val translationY: Float,
    val playerViewResizeMode: Int,
    val targetWidth: Float,
    val targetHeight: Float,
    val clampedPan: Offset
)

object VideoScaleController {

    fun calculate(input: VideoScaleInput): VideoScaleResult {
        val vw = input.viewWidth
        val vh = input.viewHeight
        if (vw <= 0f || vh <= 0f) {
            return VideoScaleResult(
                scaleX = 1f,
                scaleY = 1f,
                translationX = 0f,
                translationY = 0f,
                playerViewResizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT,
                targetWidth = 0f,
                targetHeight = 0f,
                clampedPan = Offset.Zero
            )
        }

        // Calculate effective video dimensions (accounting for SAR and rotation)
        val isRotated = (input.rotationDegrees == 90 || input.rotationDegrees == 270)
        val rawWidth = if (input.videoWidth > 0) input.videoWidth.toFloat() else 1920f
        val rawHeight = if (input.videoHeight > 0) input.videoHeight.toFloat() else 1080f
        val sar = if (input.pixelWidthHeightRatio > 0f) input.pixelWidthHeightRatio else 1.0f

        val effVideoW = if (isRotated) rawHeight * (1f / sar) else rawWidth * sar
        val effVideoH = if (isRotated) rawWidth else rawHeight
        val videoRatio = if (effVideoH > 0f) effVideoW / effVideoH else 16f / 9f

        val viewRatio = vw / vh

        // Natural FIT dimensions inside viewWidth x viewHeight
        val fitW: Float
        val fitH: Float
        if (videoRatio > viewRatio) {
            fitW = vw
            fitH = vw / videoRatio
        } else {
            fitH = vh
            fitW = vh * videoRatio
        }

        var baseScaleX = 1.0f
        var baseScaleY = 1.0f
        var baseW = fitW
        var baseH = fitH
        var resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        when (input.mode) {
            AspectRatioMode.FIT -> {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                baseScaleX = 1.0f
                baseScaleY = 1.0f
                baseW = fitW
                baseH = fitH
            }
            AspectRatioMode.STRETCH -> {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                baseScaleX = 1.0f
                baseScaleY = 1.0f
                baseW = vw
                baseH = vh
            }
            AspectRatioMode.CROP -> {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                val cropW = if (videoRatio > viewRatio) vh * videoRatio else vw
                val cropH = if (videoRatio > viewRatio) vh else vw / videoRatio
                baseScaleX = 1.0f
                baseScaleY = 1.0f
                baseW = cropW
                baseH = cropH
            }
            AspectRatioMode.ORIGINAL_100 -> {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                baseW = effVideoW
                baseH = effVideoH
                baseScaleX = if (fitW > 0f) effVideoW / fitW else 1.0f
                baseScaleY = if (fitH > 0f) effVideoH / fitH else 1.0f
            }
            AspectRatioMode.CUSTOM -> {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                val targetRatio = input.customRatio ?: videoRatio
                val customW: Float
                val customH: Float
                if (targetRatio > viewRatio) {
                    customW = vw
                    customH = vw / targetRatio
                } else {
                    customH = vh
                    customW = vh * targetRatio
                }
                baseW = customW
                baseH = customH
                baseScaleX = if (fitW > 0f) customW / fitW else 1.0f
                baseScaleY = if (fitH > 0f) customH / fitH else 1.0f
            }
        }

        // Apply user pinch zoom [1.0 .. 4.0]
        val clampedZoom = input.userZoom.coerceIn(1.0f, 4.0f)
        val finalScaleX = baseScaleX * clampedZoom
        val finalScaleY = baseScaleY * clampedZoom

        val displayedW = baseW * clampedZoom
        val displayedH = baseH * clampedZoom

        // Clamp user pan so video cannot be dragged entirely off-screen
        val maxPanX = if (displayedW > vw) (displayedW - vw) / 2f else 0f
        val maxPanY = if (displayedH > vh) (displayedH - vh) / 2f else 0f

        val clampedPanX = if (maxPanX > 0f) input.userPan.x.coerceIn(-maxPanX, maxPanX) else 0f
        val clampedPanY = if (maxPanY > 0f) input.userPan.y.coerceIn(-maxPanY, maxPanY) else 0f
        val clampedPan = Offset(clampedPanX, clampedPanY)

        return VideoScaleResult(
            scaleX = finalScaleX,
            scaleY = finalScaleY,
            translationX = clampedPan.x,
            translationY = clampedPan.y,
            playerViewResizeMode = resizeMode,
            targetWidth = displayedW,
            targetHeight = displayedH,
            clampedPan = clampedPan
        )
    }
}
