package com.npplayer.player.scale

import androidx.compose.ui.geometry.Offset
import androidx.media3.ui.AspectRatioFrameLayout
import com.npplayer.core.data.preferences.AspectRatioMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoScaleControllerTest {

    @Test
    fun testFitMode_16x9_on_19point5x9_screen() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 2340f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT
        )
        val result = VideoScaleController.calculate(input)
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(0f, result.translationX, 0.01f)
        assertEquals(0f, result.translationY, 0.01f)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, result.playerViewResizeMode)
        assertEquals(1920f, result.targetWidth, 0.1f)
        assertEquals(1080f, result.targetHeight, 0.1f)
    }

    @Test
    fun testStretchMode_fillsScreen() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 2340f,
            viewHeight = 1080f,
            mode = AspectRatioMode.STRETCH
        )
        val result = VideoScaleController.calculate(input)
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, result.playerViewResizeMode)
        assertEquals(2340f, result.targetWidth, 0.1f)
        assertEquals(1080f, result.targetHeight, 0.1f)
    }

    @Test
    fun testCropMode_fillsScreenAndMaintainsRatio() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 2340f,
            viewHeight = 1080f,
            mode = AspectRatioMode.CROP
        )
        val result = VideoScaleController.calculate(input)
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, result.playerViewResizeMode)
        // 1080 * (2340/1080 is wider than 16/9, so height = 1080 * (2340/1920) or crop horizontally
        // videoRatio = 1.777, viewRatio = 2.166. video is narrower than screen -> crop height or zoom width
        // cropW = 2340, cropH = 2340 / (16/9) = 1316.25
        assertEquals(2340f, result.targetWidth, 0.1f)
        assertEquals(1316.25f, result.targetHeight, 0.1f)
    }

    @Test
    fun test100PercentMode_720p_on_1080p_screen() {
        val input = VideoScaleInput(
            videoWidth = 1280,
            videoHeight = 720,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.ORIGINAL_100
        )
        val result = VideoScaleController.calculate(input)
        // FIT would scale 720p to 1920x1080 (1.5x)
        // 100% mode scales down by 1/1.5 = 0.6667
        assertEquals(0.6667f, result.scaleX, 0.01f)
        assertEquals(0.6667f, result.scaleY, 0.01f)
        assertEquals(1280f, result.targetWidth, 0.1f)
        assertEquals(720f, result.targetHeight, 0.1f)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, result.playerViewResizeMode)
    }

    @Test
    fun testCustomPreset_21x9() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.CUSTOM,
            customRatio = 21f / 9f
        )
        val result = VideoScaleController.calculate(input)
        // 21:9 target on 1920x1080: targetW = 1920, targetH = 1920 / (21/9) = 822.85f
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(822.857f / 1080f, result.scaleY, 0.01f)
        assertEquals(1920f, result.targetWidth, 0.1f)
        assertEquals(822.857f, result.targetHeight, 0.1f)
    }

    @Test
    fun testPinchZoomAndPanClamping() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT,
            userZoom = 2.0f,
            userPan = Offset(5000f, -5000f) // Way beyond bounds
        )
        val result = VideoScaleController.calculate(input)
        // 2.0x zoom: target width = 3840, target height = 2160
        // maxPanX = (3840 - 1920) / 2 = 960
        // maxPanY = (2160 - 1080) / 2 = 540
        assertEquals(2.0f, result.scaleX, 0.01f)
        assertEquals(2.0f, result.scaleY, 0.01f)
        assertEquals(960f, result.translationX, 0.01f)
        assertEquals(-540f, result.translationY, 0.01f)
    }

    @Test
    fun testPortraitVideo_9x16_on_landscape_screen() {
        val input = VideoScaleInput(
            videoWidth = 1080,
            videoHeight = 1920,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT
        )
        val result = VideoScaleController.calculate(input)
        // fitH = 1080, fitW = 1080 * (9/16) = 607.5
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(607.5f, result.targetWidth, 0.1f)
        assertEquals(1080f, result.targetHeight, 0.1f)
    }

    @Test
    fun testPortraitVideo_9x16_on_portrait_phone() {
        val input = VideoScaleInput(
            videoWidth = 1080,
            videoHeight = 1920,
            viewWidth = 1080f,
            viewHeight = 2340f,
            mode = AspectRatioMode.FIT
        )
        val result = VideoScaleController.calculate(input)
        // videoRatio = 9/16 = 0.5625. viewRatio = 1080/2340 = 0.4615
        // video is wider than phone: fitW = 1080, fitH = 1080 / (9/16) = 1920
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(1080f, result.targetWidth, 0.1f)
        assertEquals(1920f, result.targetHeight, 0.1f)
    }

    @Test
    fun test4x3Video_on_16x9_landscape_screen() {
        val input = VideoScaleInput(
            videoWidth = 1440,
            videoHeight = 1080,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT
        )
        val result = VideoScaleController.calculate(input)
        // 4:3 fits inside 16:9 with pillarboxes on left/right: fitH = 1080, fitW = 1440
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(1440f, result.targetWidth, 0.1f)
        assertEquals(1080f, result.targetHeight, 0.1f)
    }

    @Test
    fun test21x9Video_on_16x9_landscape_screen() {
        val input = VideoScaleInput(
            videoWidth = 2560,
            videoHeight = 1080,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT
        )
        val result = VideoScaleController.calculate(input)
        // 21:9 fits inside 16:9 with letterboxes on top/bottom: fitW = 1920, fitH = 1920 / (2560/1080) = 810
        assertEquals(1.0f, result.scaleX, 0.01f)
        assertEquals(1.0f, result.scaleY, 0.01f)
        assertEquals(1920f, result.targetWidth, 0.1f)
        assertEquals(810f, result.targetHeight, 0.1f)
    }

    @Test
    fun testMax4xZoomClamping() {
        val input = VideoScaleInput(
            videoWidth = 1920,
            videoHeight = 1080,
            viewWidth = 1920f,
            viewHeight = 1080f,
            mode = AspectRatioMode.FIT,
            userZoom = 10.0f // Attempting to zoom beyond 4x
        )
        val result = VideoScaleController.calculate(input)
        // Clamped at 4.0x
        assertEquals(4.0f, result.scaleX, 0.01f)
        assertEquals(4.0f, result.scaleY, 0.01f)
        assertEquals(1920f * 4f, result.targetWidth, 0.1f)
        assertEquals(1080f * 4f, result.targetHeight, 0.1f)
    }
}
