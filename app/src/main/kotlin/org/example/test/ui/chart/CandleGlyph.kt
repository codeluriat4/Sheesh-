package org.example.test.ui.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.example.test.domain.chart.CandleSlot
import org.example.test.domain.chart.PriceRange
import org.example.test.domain.common.Price
import org.example.test.ui.theme.DadaColorScheme

// How a slot paints itself onto the canvas. A Filled slot draws a full
// OHLC wick-and-body glyph; an Empty slot draws a thin placeholder tick.
// That difference is expressed as two implementations of this interface
// rather than a branch inside one shared draw function.
sealed interface CandleGlyph {

    fun DrawScope.draw(column: CandleColumn, priceRange: PriceRange, colors: DadaColorScheme)

    data class Filled(val slot: CandleSlot.Filled) : CandleGlyph {
        override fun DrawScope.draw(column: CandleColumn, priceRange: PriceRange, colors: DadaColorScheme) {
            val candle = slot.candle
            val color = candle.trend.colorIn(colors)

            fun yOf(price: Price): Float {
                val fraction = priceRange.fractionOf(price)
                return (column.bottomY - (fraction * (column.bottomY - column.topY))).toFloat()
            }

            drawLine(
                color = color,
                start = Offset(column.centerX, yOf(candle.high)),
                end = Offset(column.centerX, yOf(candle.low)),
                strokeWidth = WICK_STROKE_WIDTH,
            )

            val openY = yOf(candle.open)
            val closeY = yOf(candle.close)
            val bodyTop = minOf(openY, closeY)
            val bodyHeight = maxOf(kotlin.math.abs(closeY - openY), MIN_BODY_HEIGHT)

            drawRect(
                color = color,
                topLeft = Offset(column.centerX - column.bodyWidth / 2f, bodyTop),
                size = Size(column.bodyWidth, bodyHeight),
            )
        }
    }

    data class Empty(val slot: CandleSlot.Empty) : CandleGlyph {
        override fun DrawScope.draw(column: CandleColumn, priceRange: PriceRange, colors: DadaColorScheme) {
            val midY = (column.topY + column.bottomY) / 2f
            drawLine(
                color = colors.outline,
                start = Offset(column.centerX - column.bodyWidth / 2f, midY),
                end = Offset(column.centerX + column.bodyWidth / 2f, midY),
                strokeWidth = WICK_STROKE_WIDTH,
            )
        }
    }

    companion object {
        private const val WICK_STROKE_WIDTH = 2f
        private const val MIN_BODY_HEIGHT = 1f

        // Resolved via the slot's own polymorphic fold rather than an
        // is-check, so this factory never branches on the slot's type.
        fun of(slot: CandleSlot): CandleGlyph = slot.fold(
            onFilled = { filled -> Filled(filled) },
            onEmpty = { empty -> Empty(empty) },
        )
    }
}
