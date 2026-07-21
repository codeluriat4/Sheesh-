package org.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.test.ui.chart.ChartLayoutMode
import org.example.test.ui.chart.ChartScreenSlots
import org.example.test.ui.theme.DadaThemeTokens

// The application's primary full-screen container for chart-related
// content. Its single responsibility is hosting and arranging that
// content; it never renders chart data, order book rows, or controls
// itself. Each region is supplied by the caller as a slot, so adding or
// replacing a piece of chart content never requires touching this screen,
// and the arrangement itself is delegated to ChartLayoutMode rather than
// branched on here.
@Composable
fun ChartScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable (Modifier) -> Unit = {},
    chart: @Composable (Modifier) -> Unit = {},
    sidePanel: @Composable (Modifier) -> Unit = {},
    bottomBar: @Composable (Modifier) -> Unit = {},
) {
    val colors = DadaThemeTokens.colors
    val spacing = DadaThemeTokens.spacing

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(spacing.sm),
    ) {
        val slots = ChartScreenSlots(
            topBar = topBar,
            chart = chart,
            sidePanel = sidePanel,
            bottomBar = bottomBar,
        )
        ChartLayoutMode.forWidth(maxWidth).Arrange(slots)
    }
}
