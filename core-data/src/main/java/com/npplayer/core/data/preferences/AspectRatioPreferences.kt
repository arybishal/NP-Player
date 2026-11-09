package com.npplayer.core.data.preferences

enum class AspectRatioMode(val id: String, val displayName: String, val shortName: String) {
    FIT("fit", "Fit to screen", "Fit"),
    STRETCH("stretch", "Stretch to fill", "Stretch"),
    CROP("crop", "Crop to fill", "Crop"),
    ORIGINAL_100("100", "100% Original", "100%"),
    CUSTOM("custom", "Custom ratio", "Custom");

    companion object {
        fun fromId(id: String): AspectRatioMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: FIT
        }
    }
}

data class AspectRatioPreset(
    val label: String,
    val widthRatio: Float,
    val heightRatio: Float
) {
    val ratio: Float get() = if (heightRatio > 0f) widthRatio / heightRatio else 16f / 9f

    companion object {
        val PRESETS = listOf(
            AspectRatioPreset("16:9", 16f, 9f),
            AspectRatioPreset("4:3", 4f, 3f),
            AspectRatioPreset("21:9", 21f, 9f),
            AspectRatioPreset("1:1", 1f, 1f),
            AspectRatioPreset("18:9", 18f, 9f),
            AspectRatioPreset("19.5:9", 19.5f, 9f)
        )

        fun findByLabel(label: String): AspectRatioPreset {
            return PRESETS.find { it.label.equals(label, ignoreCase = true) } ?: PRESETS[0]
        }
    }
}

data class AspectRatioPreferences(
    val mode: AspectRatioMode = AspectRatioMode.FIT,
    val customPreset: String = "16:9",
    val customRatioValue: Float = 16f / 9f,
    val rememberPerVideo: Boolean = false
)
