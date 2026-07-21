package org.example.test.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.test.domain.chart.PriceAxisTicks
import org.example.test.domain.chart.PriceRange
import org.example.test.ui.theme.DadaThemeTokens

// A reusable, right-aligned vertical price scale. It knows nothing
// about candles, order books, or timeframes; it only turns a PriceRange
// into tick lines and labels along the right edge of its own bounds.
// Because it takes that range as a plain parameter rather than
// observing anything itself, it stays synchronized with whatever chart
// shares the range simply by recomposing alongside it — every new
// emission the caller collects redraws this axis in the same frame,
// with no timer or poll loop of its own.
@Composable
fun PriceAxis(
    priceRange: PriceRange,
    modifier: Modifier = Modifier,
    tickCount: Int = PriceAxisTicks.DEFAULT_TICK_COUNT,
    formatPrice: (Double) -> String = ::defaultPriceFormat,
) {
    val colors = DadaThemeTokens.colors
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = DadaThemeTokens.typography.labelMedium.copy(
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Right,
    )
    val ticks = PriceAxisTicks.of(range = priceRange, count = tickCount).ticks

    Canvas(modifier = modifier.fillMaxHeight().width(AXIS_WIDTH)) {
        val tickLineLengthPx = TICK_LINE_LENGTH.toPx()
        val labelEndPaddingPx = LABEL_END_PADDING.toPx()

        ticks.forEach { tick ->
            val y = size.height - (tick.fraction * size.height).toFloat()

            drawLine(
                color = colors.outline,
                start = Offset(0f, y),
                end = Offset(tickLineLengthPx, y),
                strokeWidth = TICK_STROKE_WIDTH,
            )

            val layout = textMeasurer.measure(text = formatPrice(tick.price.value), style = labelStyle)
            val labelTop = (y - layout.size.height / 2f).coerceIn(0f, size.height - layout.size.height)

            drawText(
                textLayoutResult = layout,
                topLeft = Offset(x = size.width - layout.size.width - labelEndPaddingPx, y = labelTop),
            )
        }
    }
}

private fun defaultPriceFormat(value: Double): String = "%,.2f".format(value)

private val AXIS_WIDTH = 64.dp
private val TICK_LINE_LENGTH = 6.dp
private val LABEL_END_PADDING = 4.dp
private const val TICK_STROKE_WIDTH = 1f
