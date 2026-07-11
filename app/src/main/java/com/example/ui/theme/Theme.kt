package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AppleBlue,
    secondary = AppleStaticMutedGrey,
    tertiary = AppleGreen,
    background = AppleStaticBlack,
    surface = AppleStaticCharcoal,
    onPrimary = AppleStaticLightGrey,
    onBackground = AppleStaticLightGrey,
    onSurface = AppleStaticLightGrey,
    surfaceVariant = AppleStaticGlassCard,
    error = AppleRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AppleBlue,
    secondary = AppleStaticMutedGrey,
    tertiary = AppleGreen,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF161617),
    onBackground = Color(0xFF161617),
    onSurface = Color(0xFF161617),
    surfaceVariant = Color(0xFFE5E5EA),
    error = AppleRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(
    LocalDarkTheme provides darkTheme
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
