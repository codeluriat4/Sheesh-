package org.example.test.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.example.test.ui.theme.DadaThemeTokens

// A single pulsing placeholder shape. Every skeleton layout in the app is
// built out of this one primitive, so the shimmer animation only ever
// needs to be tuned in one place.
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = DadaThemeTokens.shapes.small,
) {
    val colors = DadaThemeTokens.colors
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceVariant.copy(alpha = alpha), shape),
    )
}
