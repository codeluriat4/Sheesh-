package org.example.test.ui.theme

import androidx.compose.ui.graphics.Color

// The only place RgbColor learns about Compose; gradient math stays pure.
fun RgbColor.toComposeColor(alpha: Float = 1f): Color =
    Color(red = red / 255f, green = green / 255f, blue = blue / 255f, alpha = alpha)
