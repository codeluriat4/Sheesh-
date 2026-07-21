package org.example.test.ui.chart

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.example.test.R
import org.example.test.ui.theme.DadaThemeTokens

// The chart screen's topBar slot content. Its single responsibility is
// showing the screen title; it never renders chart data or controls.
@Composable
fun ChartTopBar(modifier: Modifier = Modifier) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography
    val spacing = DadaThemeTokens.spacing

    BasicText(
        text = stringResource(R.string.chart_screen_title),
        style = typography.headlineMedium.copy(color = colors.onSurface),
        modifier = modifier.padding(spacing.sm),
    )
}
