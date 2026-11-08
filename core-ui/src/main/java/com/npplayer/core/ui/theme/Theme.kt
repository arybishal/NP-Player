package com.npplayer.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode {
    FOLLOW_DEVICE,
    LIGHT,
    DARK
}

// True AMOLED Dark Theme (Pure black)
val NPDarkColorScheme = darkColorScheme(
    primary = NPYellow,
    onPrimary = NPBlack,
    primaryContainer = NPYellow.copy(alpha = 0.25f),
    onPrimaryContainer = NPYellow,
    secondary = NPWhite,
    onSecondary = NPBlack,
    secondaryContainer = NPWhite.copy(alpha = 0.12f),
    onSecondaryContainer = NPWhite,
    background = NPBlack,
    onBackground = NPWhite,
    surface = NPBlack,
    onSurface = NPWhite,
    surfaceVariant = NPWhite.copy(alpha = 0.08f),
    onSurfaceVariant = NPWhite.copy(alpha = 0.70f),
    outline = NPWhite.copy(alpha = 0.18f),
    outlineVariant = NPWhite.copy(alpha = 0.10f)
)

// High-contrast Light Theme (Pure white)
val NPLightColorScheme = lightColorScheme(
    primary = NPYellow,
    onPrimary = NPBlack,
    primaryContainer = NPYellow.copy(alpha = 0.20f),
    onPrimaryContainer = NPBlack,
    secondary = NPBlack,
    onSecondary = NPWhite,
    secondaryContainer = NPBlack.copy(alpha = 0.08f),
    onSecondaryContainer = NPBlack,
    background = NPWhite,
    onBackground = NPBlack,
    surface = NPWhite,
    onSurface = NPBlack,
    surfaceVariant = NPBlack.copy(alpha = 0.05f),
    onSurfaceVariant = NPBlack.copy(alpha = 0.70f),
    outline = NPBlack.copy(alpha = 0.15f),
    outlineVariant = NPBlack.copy(alpha = 0.08f)
)

@Composable
fun NPPlayerTheme(
    themeMode: ThemeMode = ThemeMode.FOLLOW_DEVICE,
    forceDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when {
        forceDark -> true
        themeMode == ThemeMode.DARK -> true
        themeMode == ThemeMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) NPDarkColorScheme else NPLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NPTypography,
        shapes = NPShapes,
        content = content
    )
}
