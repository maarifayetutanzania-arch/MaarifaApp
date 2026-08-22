package com.maarifa.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colors
val MaarifaForest = Color(0xFF1B4332)
val MaarifaForestTint = Color(0xFF40916C)
val MaarifaForestDeep = Color(0xFF081C15)

val MaarifaGold = Color(0xFFFFD700)
val MaarifaGoldTint = Color(0xFFFFF3BF)

val MaarifaClay = Color(0xFFB7094C)
val MaarifaClayTint = Color(0xFFE07A5F)

val MaarifaSurface = Color(0xFFFFFFFF)
val MaarifaPaper = Color(0xFFF8F9FA)
val MaarifaInk = Color(0xFF212529)
val MaarifaInkSoft = Color(0xFF6C757D)
val MaarifaLine = Color(0xFFDEE2E6)

private val LightColorScheme = lightColorScheme(
    primary = MaarifaForest,
    onPrimary = MaarifaSurface,
    primaryContainer = MaarifaForestTint,
    onPrimaryContainer = MaarifaForestDeep,

    secondary = MaarifaGold,
    onSecondary = MaarifaSurface,
    secondaryContainer = MaarifaGoldTint,
    onSecondaryContainer = MaarifaInk,

    tertiary = MaarifaClay,
    onTertiary = MaarifaSurface,
    tertiaryContainer = MaarifaClayTint,
    onTertiaryContainer = MaarifaInk,

    background = MaarifaPaper,
    onBackground = MaarifaInk,
    surface = MaarifaSurface,
    onSurface = MaarifaInk,
    surfaceVariant = MaarifaPaper,
    onSurfaceVariant = MaarifaInkSoft,

    outline = MaarifaLine,
    outlineVariant = MaarifaLine
)

private val DarkColorScheme = darkColorScheme(
    primary = MaarifaForestTint,
    onPrimary = MaarifaForestDeep,
    primaryContainer = MaarifaForest,
    onPrimaryContainer = MaarifaForestTint,

    secondary = MaarifaGold,
    onSecondary = MaarifaInk,
    secondaryContainer = MaarifaForestDeep,
    onSecondaryContainer = MaarifaGoldTint,

    background = MaarifaForestDeep,
    onBackground = MaarifaPaper,
    surface = MaarifaInk,
    onSurface = MaarifaPaper,
    surfaceVariant = MaarifaForestDeep,
    onSurfaceVariant = MaarifaInkSoft,

    outline = MaarifaInkSoft
)

@Composable
fun MaarifaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MaarifaShapes,
        content = content
    )
}
