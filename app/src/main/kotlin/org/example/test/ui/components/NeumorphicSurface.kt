package org.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.example.test.ui.theme.DadaElevation
import org.example.test.ui.theme.DadaThemeTokens
import org.example.test.ui.theme.dadaElevation

// The one container every screen composes with to get a themed, elevated
// surface. Screens never draw their own shadows or pick their own colors,
// so the whole app inherits one visual language automatically.
@Composable
fun NeumorphicSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = DadaThemeTokens.shapes.medium,
    elevation: DadaElevation = DadaElevation.Raised,
    content: @Composable () -> Unit,
) {
    val colors = DadaThemeTokens.colors
    Box(
        modifier = modifier
            .dadaElevation(elevation = elevation, shape = shape)
            .clip(shape)
            .background(colors.surface, shape),
    ) {
        content()
    }
}
