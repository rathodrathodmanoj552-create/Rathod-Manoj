package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpiderDarkColorScheme = darkColorScheme(
    primary = SpiderRed,
    onPrimary = Color.White,
    primaryContainer = SpiderRedDark,
    onPrimaryContainer = SpiderRedGlow,
    secondary = SpiderElectricBlue,
    onSecondary = SpiderNavyDark,
    secondaryContainer = SpiderNavyElevated,
    onSecondaryContainer = SpiderElectricBlue,
    tertiary = SpiderGold,
    onTertiary = SpiderNavyDark,
    background = SpiderNavy,
    onBackground = SpiderTextPrimary,
    surface = SpiderNavySurface,
    onSurface = SpiderTextPrimary,
    surfaceVariant = SpiderNavyElevated,
    onSurfaceVariant = SpiderTextSecondary,
    outline = SpiderNavyBorder,
    outlineVariant = SpiderElectricBlue.copy(alpha = 0.3f),
    error = SpiderRedBright,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our superhero Spider-Man theme by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpiderDarkColorScheme,
        typography = Typography,
        content = content
    )
}
