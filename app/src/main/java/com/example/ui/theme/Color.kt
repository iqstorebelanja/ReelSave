package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Linear + Raycast Pure Black & Neon Palette
val PureBlack = Color(0xFF000000)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0A0A0C)
val DarkSurfaceVariant = Color(0xFF121216)

// Glassmorphism tokens: bg-white/[0.03] border-white/[0.06]
val GlassBg = Color(0x0AFFFFFF)          // ~3.9% white
val GlassBgCard = Color(0x0DFFFFFF)      // ~5.1% white
val GlassBgElevated = Color(0x14FFFFFF)  // ~7.8% white
val GlassBorder = Color(0x10FFFFFF)      // ~6.3% white
val GlassBorderHover = Color(0x20FFFFFF) // ~12.5% white
val GlassBorderGlow = Color(0x4000D1FF)  // 25% neon cyan glow

// Neon Blue (#00D1FF) Accent for Active States
val NeonBlue = Color(0xFF00D1FF)
val NeonBlueDark = Color(0xFF0094C6)
val NeonPurple = Color(0xFF8A2BE2)

// Text Hierarchy (Linear/Raycast typography)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8A8F98)
val TextTertiary = Color(0xFF525866)

// Status Accents
val AccentSuccess = Color(0xFF00E599)
val AccentWarning = Color(0xFFF5A623)
val AccentError = Color(0xFFFF4D4F)
val VipGold = Color(0xFFFFB800)

// Instagram & Brand Highlights
val IgOrange = Color(0xFFF58529)
val IgPink = Color(0xFFDD2A7B)
val IgPurple = Color(0xFF8134AF)
val IgBlue = Color(0xFF515BD4)

// Gradient Tokens
val NeonBlueGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00D1FF), Color(0xFF0070F3))
)

val LinearGlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0x0FFFFFFF), Color(0x04FFFFFF))
)

val VipGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFB800), Color(0xFFFF5252))
)

val InstagramButtonBrush = Brush.horizontalGradient(
    colors = listOf(IgOrange, IgPink, IgBlue)
)

val InstagramGradientBrush = Brush.linearGradient(
    colors = listOf(IgOrange, IgPink, IgPurple, IgBlue)
)

val DarkCardBorder = GlassBorder
val DarkCardBorderGlow = GlassBorderGlow
