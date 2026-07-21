package org.example.test.domain.heatmap

import org.example.test.domain.chart.PriceRange
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AggregatedVolume
import org.example.test.domain.orderbook.OrderBookSample

// The fixed set of price rows a heatmap column is bucketed into, always
// derived from the same PriceRange the candlestick chart beneath it is
// drawn against. Bucketing behavior lives here, next to the range and row
// count it is computed from, so an overlay renderer and the candle
// glyphs it sits on top of can never disagree about where a price sits
// vertically.
data class HeatmapPriceGrid(val priceRange: PriceRange, val rowCount: Int) {
    init {
        require(rowCount > 0) { "rowCount must be positive, was $rowCount" }
    }

    private val rowSpan: Double get() = priceRange.span / rowCount

    // The row a price falls into, clamped to this grid's own bounds.
    fun bucketIndexOf(price: Price): Int {
        if (rowSpan <= 0.0) return 0
        val index = ((price.value - priceRange.low.value) / rowSpan).toInt()
        return index.coerceIn(0, rowCount - 1)
    }

    // The representative price at the vertical center of a row, used as
    // the AggregatedVolume key for that row's cell.
    fun bucketCenter(index: Int): Price {
        val rowLow = priceRange.low.value + (index * rowSpan)
        return Price(rowLow + rowSpan / 2.0)
    }

    // Buckets one order book sample's bid and ask levels into this grid's
    // rows, summing whatever volume falls into the same row on each side.
    fun aggregate(sample: OrderBookSample): List<AggregatedVolume> {
        val bidVolumeByRow = HashMap<Int, Double>()
        val askVolumeByRow = HashMap<Int, Double>()

        sample.bids.forEach { level ->
            bidVolumeByRow.merge(bucketIndexOf(level.price), level.volume.value, Double::plus)
        }
        sample.asks.forEach { level ->
            askVolumeByRow.merge(bucketIndexOf(level.price), level.volume.value, Double::plus)
        }

        return (bidVolumeByRow.keys + askVolumeByRow.keys).distinct().map { row ->
            AggregatedVolume(
                bucket = bucketCenter(row),
                bidVolume = Volume(bidVolumeByRow[row] ?: 0.0),
                askVolume = Volume(askVolumeByRow[row] ?: 0.0),
            )
        }
    }
}
