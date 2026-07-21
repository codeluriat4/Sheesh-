package org.example.test.ui.theme

import androidx.compose.ui.graphics.Color

// Data + behavior stay together: a color scheme is just data, but it is the
// single source of truth every screen and component reads from.
data class DadaColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
    // Neumorphism renders elevation as a pair of soft shadows rather than a
    // drop shadow, so the palette carries both explicitly.
    val shadowLight: Color,
    val shadowDark: Color,
    // Directional candle colors, carried on the palette like every other
    // semantic color so charts never pick their own greens and reds.
    val bullish: Color,
    val bearish: Color,
)

// The application is dark-only by design, so a single immutable palette is
// the whole design system's color source; there is no light/dark branch.
val DadaDarkColorScheme = DadaColorScheme(
    background = Color(0xFF1C1F26),
    surface = Color(0xFF20242C),
    surfaceVariant = Color(0xFF262B34),
    primary = Color(0xFF7C9CFF),
    onPrimary = Color(0xFF0B1020),
    onSurface = Color(0xFFE7E9EE),
    onSurfaceVariant = Color(0xFFA6ACBB),
    outline = Color(0xFF333944),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0A0A),
    shadowLight = Color(0xFF2E333D),
    shadowDark = Color(0xFF14161B),
    bullish = Color(0xFF3DDC97),
    bearish = Color(0xFFFF6B6B),
)
