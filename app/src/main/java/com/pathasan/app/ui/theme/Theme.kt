package com.pathasan.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PathAsanColors = lightColorScheme(
    primary = PathAsanRed,
    onPrimary = PathAsanWhite,
    secondary = PathAsanBlack,
    onSecondary = PathAsanWhite,
    background = PathAsanLightGray,
    onBackground = PathAsanBlack,
    surface = PathAsanWhite,
    onSurface = PathAsanBlack,
    error = PathAsanRed,
    outline = PathAsanLine,
    surfaceVariant = PathAsanLightGray,
    onSurfaceVariant = PathAsanMuted,
    tertiary = PathAsanRed,
    scrim = Color.Black,
)

@Composable
fun PathAsanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PathAsanColors,
        typography = Typography(),
        content = content,
    )
}
