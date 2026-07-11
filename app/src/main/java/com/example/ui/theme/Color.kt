package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Composition local for dark theme toggle
val LocalDarkTheme = compositionLocalOf { true }

// Apple Premium Kiosk Dark Palette (Static values for theme customization)
val AppleStaticBlack = Color(0xFF000000)
val AppleStaticCharcoal = Color(0xFF161617)
val AppleStaticGlassCard = Color(0xFF1C1C1E)
val AppleStaticLightGrey = Color(0xFFF5F5F7)
val AppleStaticMutedGrey = Color(0xFF8E8E93)

// Static accent colors
val AppleBlue = Color(0xFF0071E3)
val AppleGreen = Color(0xFF30D158)
val AppleRed = Color(0xFFFF453A)
val AppleAmber = Color(0xFFFF9F0A)
val AppleDivider = Color(0xFF2C2C2E)

// Dynamic Cupertino-styled colors based on dark/light theme state
val AppleBlack: Color
  @Composable
  get() = if (LocalDarkTheme.current) Color(0xFF000000) else Color(0xFFFAFAFA) // Pure black in dark, pristine clean light gray in light mode

val AppleCharcoal: Color
  @Composable
  get() = if (LocalDarkTheme.current) Color(0xFF161617) else Color(0xFFFFFFFF) // Dark slate card background in dark, pure clean white card background in light mode

val AppleLightGrey: Color
  @Composable
  get() = if (LocalDarkTheme.current) Color(0xFFF5F5F7) else Color(0xFF111112) // Near-white text in dark mode, near-black/charcoal dark text in light mode

val AppleMutedGrey: Color
  @Composable
  get() = if (LocalDarkTheme.current) Color(0xFF8E8E93) else Color(0xFF636366) // Soft medium gray in dark, solid readable muted slate in light mode

val AppleBorderColor: Color
  @Composable
  get() = if (LocalDarkTheme.current) Color(0xFF2C2C2E) else Color(0xFFE5E5EA) // Dark screen divider in dark mode, crisp clean thin gray separator in light mode
