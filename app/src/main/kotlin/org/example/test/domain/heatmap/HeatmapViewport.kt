package org.example.test.domain.heatmap

import org.example.test.domain.chart.PriceRange
import org.example.test.domain.orderbook.OrderBookSample

// The single place that decides how far a heatmap's own price axis must
// extend. A CandlestickWindow's PriceRange only covers where price
// traded, but resting liquidity routinely sits above or below that band;
// bucketing such a level against the narrower candle range would clamp
// it onto an edge row, silently merging it with whatever else already
// lives there. This type instead grows the range to enclose every price
// level actually present in the window's own order book history, so a
// HeatmapPriceGrid built from its output never needs to clamp a real
// node away from its true row. Candles remain the range's floor: a
// window with no book history yet keeps exactly the candle range it
// already had.
object HeatmapViewport {

    fun scaledRange(candleRange: PriceRange, samples: List<OrderBookSample>): PriceRange {
        val levelPrices = samples.asSequence().flatMap { sample ->
            sample.bids.asSequence().map { it.price } + sample.asks.asSequence().map { it.price }
        }

        var low = candleRange.low
        var high = candleRange.high
        levelPrices.forEach { price ->
            if (price < low) low = price
            if (price > high) high = price
        }

        return if (low == candleRange.low && high == candleRange.high) candleRange else PriceRange(low = low, high = high)
    }
}
