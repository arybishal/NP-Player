package com.npplayer.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PicturePreferencesTest {

    @Test
    fun defaultSettings_isDefaultReturnsTrue() {
        val settings = PictureSettings.DEFAULT
        assertTrue(settings.isDefault())
        assertEquals(0, settings.brightnessUi)
        assertEquals(0, settings.contrastUi)
        assertEquals(0, settings.saturationUi)
        assertEquals(1.0f, settings.gammaUi, 0.01f)
        assertEquals(0, settings.sharpnessUi)
        assertEquals(0, settings.warmthUi)
        assertEquals(0, settings.hueUi)
    }

    @Test
    fun moviePreset_hasExpectedValues() {
        val movie = PictureSettings.fromPreset(PicturePreset.MOVIE)
        assertFalse(movie.isDefault())
        assertEquals(PicturePreset.MOVIE, movie.activePreset)
        assertEquals(15, movie.contrastUi)
        assertEquals(8, movie.saturationUi)
        assertEquals(1.1f, movie.gammaUi, 0.05f)
        assertEquals(20, movie.warmthUi)
    }

    @Test
    fun animePreset_hasEnhancedSaturationAndSharpness() {
        val anime = PictureSettings.fromPreset(PicturePreset.ANIME)
        assertEquals(PicturePreset.ANIME, anime.activePreset)
        assertEquals(35, anime.saturationUi)
        assertEquals(35, anime.sharpnessUi)
        assertEquals(15, anime.contrastUi)
    }

    @Test
    fun nightPreset_hasStrongWarmFilterAndLowerBrightness() {
        val night = PictureSettings.fromPreset(PicturePreset.NIGHT)
        assertEquals(PicturePreset.NIGHT, night.activePreset)
        assertEquals(60, night.warmthUi)
        assertEquals(-15, night.brightnessUi)
    }

    @Test
    fun sharpPreset_hasHighSharpness() {
        val sharp = PictureSettings.fromPreset(PicturePreset.SHARP)
        assertEquals(PicturePreset.SHARP, sharp.activePreset)
        assertEquals(65, sharp.sharpnessUi)
    }

    @Test
    fun uiValueConversion_mapsAccurately() {
        val converted = PictureSettings.fromUiValues(
            brightnessUi = 20,
            contrastUi = -10,
            saturationUi = 40,
            gammaUi = 1.3f,
            sharpnessUi = 50,
            warmthUi = 30,
            hueUi = 45,
            rememberPerVideo = true,
            includeFiltersInScreenshot = true
        )

        assertEquals(0.20f, converted.brightness, 0.01f)
        assertEquals(0.90f, converted.contrast, 0.01f)
        assertEquals(1.40f, converted.saturation, 0.01f)
        assertEquals(1.3f, converted.gamma, 0.01f)
        assertEquals(0.50f, converted.sharpness, 0.01f)
        assertEquals(0.30f, converted.warmth, 0.01f)
        assertEquals(45f, converted.hue, 0.01f)
        assertTrue(converted.rememberPerVideo)
        assertTrue(converted.includeFiltersInScreenshot)
    }

    @Test
    fun uiValueConversion_clampsToBounds() {
        val clamped = PictureSettings.fromUiValues(
            brightnessUi = 150,
            contrastUi = -150,
            saturationUi = 200,
            gammaUi = 3.5f,
            sharpnessUi = 150,
            warmthUi = -20,
            hueUi = 300,
            rememberPerVideo = false,
            includeFiltersInScreenshot = false
        )

        assertEquals(1.0f, clamped.brightness, 0.01f)
        assertEquals(0.0f, clamped.contrast, 0.01f)
        assertEquals(2.0f, clamped.saturation, 0.01f)
        assertEquals(2.5f, clamped.gamma, 0.01f)
        assertEquals(1.0f, clamped.sharpness, 0.01f)
        assertEquals(0.0f, clamped.warmth, 0.01f)
        assertEquals(180f, clamped.hue, 0.01f)
    }
}
