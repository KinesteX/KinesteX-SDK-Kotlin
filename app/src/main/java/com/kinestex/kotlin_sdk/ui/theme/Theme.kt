package com.kinestex.kotlin_sdk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KinesteXBlue = Color(0xFF2196F3)        // iOS Blue
private val KinesteXLightGray = Color(0xFFF5F5F5)   // Background
private val KinesteXCardGray = Color(0xFFF2F2F7)
private val KinesteXText = Color(0xFF1C1C1E)
private val KinesteXSecondaryText = Color(0xFF8E8E93)

private val LightColorScheme = lightColorScheme(
    primary = KinesteXBlue,
    onPrimary = Color.White,
    primaryContainer = KinesteXBlue.copy(alpha = 0.1f),
    background = KinesteXLightGray,
    surface = Color.White,
    onBackground = KinesteXText,
    onSurface = KinesteXText,
    secondary = Color.Gray,
    onSecondary = Color.White,
    outline = Color.LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = KinesteXBlue,
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF2C2C2E),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun KinesteXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}