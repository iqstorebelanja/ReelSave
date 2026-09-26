package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003852),
    onPrimaryContainer = NeonBlue,
    secondary = Color(0xFF8A2BE2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1C38),
    onSecondaryContainer = Color(0xFFD4B0FF),
    tertiary = VipGold,
    onTertiary = Color.Black,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = PureBlack,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = AccentError,
    onError = Color.White
)

@Composable
fun ReelsSaveTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AllVidTheme(
    content: @Composable () -> Unit
) {
    ReelsSaveTheme(content = content)
}
