package com.teammonarch.butterfly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = CardWhite,
    secondary = SkyBlueDark,
    onSecondary = TextInk,
    background = LavenderPale,
    surface = CardWhite,
    onBackground = TextInk,
    onSurface = TextInk,
    onSurfaceVariant = TextMuted,
    error = MonarchRed
)

private val DarkColors = darkColorScheme(
    primary = VioletLight,
    onPrimary = TextInk,
    secondary = SkyBlueDark,
    onSecondary = TextInk,
    error = MonarchRed
)

// Gradients used on branded screens (Login / Sign Up)
val AuthGradient = Brush.verticalGradient(listOf(DeepViolet, Violet, LavenderLight))

// Gradient used on the patient/clinician home header
val HomeGradient = Brush.verticalGradient(listOf(SkyBlueDark, SkyBlueLight))

@Composable
fun ButterflyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
