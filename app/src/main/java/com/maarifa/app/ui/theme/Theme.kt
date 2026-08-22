package com.maarifa.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
        typography = Typography,
        shapes = MaarifaShapes,
        content = content
    )
}
