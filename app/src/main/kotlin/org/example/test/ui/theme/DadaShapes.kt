package org.example.test.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Neumorphism reads as soft, continuous curvature, so radii trend larger
// than a typical flat design system.
data class DadaShapes(
    val extraSmall: RoundedCornerShape,
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
    val extraLarge: RoundedCornerShape,
    val full: RoundedCornerShape,
)

val DadaDefaultShapes = DadaShapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
    full = RoundedCornerShape(percent = 50),
)
