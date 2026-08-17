package com.maarifa.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor

private val LightColors = lightColorScheme(
    primary = MaarifaForest,
    onPrimary = ComposeColor.White,
    primaryContainer = MaarifaForestTint,
    onPrimaryContainer = MaarifaForestDeep,
    secondary = MaarifaGold,
    secondaryContainer = MaarifaGoldTint,
    background = MaarifaPaper,
    surface = MaarifaSurface,
    surfaceVariant = MaarifaForestTint,
    error = MaarifaClay,
    errorContainer = MaarifaClayTint,
    onBackground = MaarifaInk,
    onSurface = MaarifaInk,
    onSurfaceVariant = MaarifaInkSoft,
    outline = MaarifaLine
)

private val DarkColors = darkColorScheme(
    primary = MaarifaForest,
    secondary = MaarifaGold,
    background = MaarifaForestDeep,
    surface = MaarifaForestDeep
)

@Composable
fun MaarifaTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, typography = MaarifaTypography, shapes = MaarifaShapes, content = content)
}
