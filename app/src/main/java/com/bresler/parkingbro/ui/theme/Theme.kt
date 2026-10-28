package com.bresler.parkingbro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette. Logo/original green is 0xFF00A651; darkened here per preference.
// Single token — drives the app bar, buttons, FAB and dialog accents. Tune freely.
val Green = Color(0xFF007A3C)
val DarkBackground = Color(0xFF282A36)
val AppBlack = Color(0xFF000000)
val AppWhite = Color(0xFFFFFFFF)

private val ParkingColors = darkColorScheme(
    primary = Green,
    onPrimary = AppWhite,
    background = DarkBackground,
    onBackground = AppWhite,
    surface = DarkBackground,
    onSurface = AppWhite,
)

/**
 * The app always rendered green-on-dark in both light and dark mode (colors were
 * hardcoded in XML), so the scheme is intentionally fixed to preserve that look.
 */
@Composable
fun ParkingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ParkingColors, content = content)
}
