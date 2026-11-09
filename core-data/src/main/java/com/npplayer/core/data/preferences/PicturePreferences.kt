package com.npplayer.core.data.preferences

import kotlin.math.roundToInt

enum class PicturePreset(val id: String, val title: String) {
    DEFAULT("default", "Default"),
    MOVIE("movie", "Movie"),
    ANIME("anime", "Anime"),
    VIVID("vivid", "Vivid"),
    NIGHT("night", "Night"),
    SHARP("sharp", "Sharp")
}

data class PictureSettings(
    val brightness: Float = 0.0f,     // -1.0f to 1.0f (0.0f = default)
    val contrast: Float = 1.0f,       // 0.0f to 2.0f (1.0f = default)
    val saturation: Float = 1.0f,     // 0.0f to 2.0f (1.0f = default)
    val gamma: Float = 1.0f,          // 0.5f to 2.5f (1.0f = default)
    val sharpness: Float = 0.0f,      // 0.0f to 1.0f (0.0f = default)
    val warmth: Float = 0.0f,         // 0.0f to 1.0f (0.0f = default, yellow/amber night filter)
    val hue: Float = 0.0f,            // -180.0f to 180.0f (0.0f = default)
    val activePreset: PicturePreset = PicturePreset.DEFAULT,
    val rememberPerVideo: Boolean = true,
    val includeFiltersInScreenshot: Boolean = false
) {
    fun isDefault(): Boolean {
        return brightness == 0.0f &&
                contrast == 1.0f &&
                saturation == 1.0f &&
                gamma == 1.0f &&
                sharpness == 0.0f &&
                warmth == 0.0f &&
                hue == 0.0f
    }

    // Convert normalized floats to user-friendly UI values
    val brightnessUi: Int get() = (brightness * 100).roundToInt().coerceIn(-100, 100)
    val contrastUi: Int get() = ((contrast - 1.0f) * 100).roundToInt().coerceIn(-100, 100)
    val saturationUi: Int get() = ((saturation - 1.0f) * 100).roundToInt().coerceIn(-100, 100)
    val gammaUi: Float get() = ((gamma * 10).roundToInt() / 10f).coerceIn(0.5f, 2.5f)
    val sharpnessUi: Int get() = (sharpness * 100).roundToInt().coerceIn(0, 100)
    val warmthUi: Int get() = (warmth * 100).roundToInt().coerceIn(0, 100)
    val hueUi: Int get() = hue.roundToInt().coerceIn(-180, 180)

    companion object {
        val DEFAULT = PictureSettings()

        fun fromPreset(preset: PicturePreset): PictureSettings {
            return when (preset) {
                PicturePreset.DEFAULT -> DEFAULT.copy(activePreset = PicturePreset.DEFAULT)
                PicturePreset.MOVIE -> PictureSettings(
                    brightness = 0.0f,
                    contrast = 1.15f,
                    saturation = 1.08f,
                    gamma = 1.10f,
                    sharpness = 0.12f,
                    warmth = 0.20f,
                    hue = 0.0f,
                    activePreset = PicturePreset.MOVIE
                )
                PicturePreset.ANIME -> PictureSettings(
                    brightness = 0.04f,
                    contrast = 1.15f,
                    saturation = 1.35f,
                    gamma = 1.00f,
                    sharpness = 0.35f,
                    warmth = 0.0f,
                    hue = 0.0f,
                    activePreset = PicturePreset.ANIME
                )
                PicturePreset.VIVID -> PictureSettings(
                    brightness = 0.03f,
                    contrast = 1.25f,
                    saturation = 1.40f,
                    gamma = 1.00f,
                    sharpness = 0.20f,
                    warmth = 0.0f,
                    hue = 0.0f,
                    activePreset = PicturePreset.VIVID
                )
                PicturePreset.NIGHT -> PictureSettings(
                    brightness = -0.15f,
                    contrast = 0.95f,
                    saturation = 0.90f,
                    gamma = 0.95f,
                    sharpness = 0.0f,
                    warmth = 0.60f,
                    hue = 0.0f,
                    activePreset = PicturePreset.NIGHT
                )
                PicturePreset.SHARP -> PictureSettings(
                    brightness = 0.0f,
                    contrast = 1.10f,
                    saturation = 1.05f,
                    gamma = 1.00f,
                    sharpness = 0.65f,
                    warmth = 0.0f,
                    hue = 0.0f,
                    activePreset = PicturePreset.SHARP
                )
            }
        }

        fun fromUiValues(
            brightnessUi: Int,
            contrastUi: Int,
            saturationUi: Int,
            gammaUi: Float,
            sharpnessUi: Int,
            warmthUi: Int,
            hueUi: Int,
            rememberPerVideo: Boolean,
            includeFiltersInScreenshot: Boolean
        ): PictureSettings {
            return PictureSettings(
                brightness = (brightnessUi / 100f).coerceIn(-1.0f, 1.0f),
                contrast = (1.0f + contrastUi / 100f).coerceIn(0.0f, 2.0f),
                saturation = (1.0f + saturationUi / 100f).coerceIn(0.0f, 2.0f),
                gamma = gammaUi.coerceIn(0.5f, 2.5f),
                sharpness = (sharpnessUi / 100f).coerceIn(0.0f, 1.0f),
                warmth = (warmthUi / 100f).coerceIn(0.0f, 1.0f),
                hue = hueUi.toFloat().coerceIn(-180.0f, 180.0f),
                activePreset = PicturePreset.DEFAULT,
                rememberPerVideo = rememberPerVideo,
                includeFiltersInScreenshot = includeFiltersInScreenshot
            )
        }
    }
}
