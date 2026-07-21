package org.example.test.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.ui.theme.DadaThemeTokens

// Renders a CandlestickWindow as evenly spaced candle columns filling the
// available canvas. A window is always exactly VisibleCandleCount.VALUE
// slots regardless of which timeframe it was built from, so this
// composable never branches on timeframe and always lays out precisely
// that many columns.
@Composable
fun CandlestickChart(
    window: CandlestickWindow,
    modifier: Modifier = Modifier,
) {
    val colors = DadaThemeTokens.colors
    val priceRange = window.priceRange

    Canvas(modifier = modifier.fillMaxSize()) {
        val columnWidth = size.width / VisibleCandleCount.VALUE
        val bodyWidth = columnWidth * BODY_WIDTH_RATIO

        window.slots.forEachIndexed { index, slot ->
            val column = CandleColumn(
                centerX = columnWidth * index + columnWidth / 2f,
                bodyWidth = bodyWidth,
                topY = 0f,
                bottomY = size.height,
            )
            with(CandleGlyph.of(slot)) {
                draw(column = column, priceRange = priceRange, colors = colors)
            }
        }
    }
}

private const val BODY_WIDTH_RATIO = 0.7f
