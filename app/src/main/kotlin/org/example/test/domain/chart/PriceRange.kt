package org.example.test.domain.chart

import org.example.test.domain.common.Price

// The low/high band a chart's price axis must cover, plus the arithmetic
// every renderer needs to place a price within it. Lives next to the two
// values it is derived from instead of being recomputed at each call site.
data class PriceRange(val low: Price, val high: Price) {
    init {
        require(low <= high) { "low cannot exceed high" }
    }

    val span: Double get() = high.value - low.value

    // Fraction from the bottom (0.0) to the top (1.0) of the range; falls
    // back to the vertical midpoint when the range has zero span so a
    // flat window never divides by zero.
    fun fractionOf(price: Price): Double =
        if (span == 0.0) 0.5 else ((price.value - low.value) / span).coerceIn(0.0, 1.0)

    companion object {
        private val FALLBACK = PriceRange(low = Price(1.0), high = Price(1.0))

        // The band spanning every high/low in the given candles, or a
        // degenerate fallback range when there are none to measure yet.
        fun of(candles: Collection<Candlestick>): PriceRange {
            if (candles.isEmpty()) return FALLBACK
            return PriceRange(
                low = Price(candles.minOf { it.low.value }),
                high = Price(candles.maxOf { it.high.value }),
            )
        }
    }
}
