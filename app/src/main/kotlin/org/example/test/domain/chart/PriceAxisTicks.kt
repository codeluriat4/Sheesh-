package org.example.test.domain.chart

import org.example.test.domain.common.Price

// A single label on a price axis: the price it names and the
// range-relative fraction (0.0 at the range's low, 1.0 at its high)
// needed to place it, so a renderer never recomputes either from the
// other.
data class PriceAxisTick(val price: Price, val fraction: Double)

// A fixed number of ticks evenly spaced across a PriceRange, from low to
// high inclusive. Deriving this here, once, means any axis renderer for
// any chart backed by a PriceRange reads the same ticks rather than
// each computing its own spacing.
data class PriceAxisTicks(val range: PriceRange, val ticks: List<PriceAxisTick>) {

    companion object {
        const val DEFAULT_TICK_COUNT = 5

        // count must be at least 2 so both bounds of the range are
        // always represented by a tick.
        fun of(range: PriceRange, count: Int = DEFAULT_TICK_COUNT): PriceAxisTicks {
            require(count >= 2) { "count must be at least 2, was $count" }

            val ticks = (0 until count).map { index ->
                val fraction = index.toDouble() / (count - 1)
                PriceAxisTick(
                    price = Price(range.low.value + fraction * range.span),
                    fraction = fraction,
                )
            }
            return PriceAxisTicks(range = range, ticks = ticks)
        }
    }
}
