package org.example.test.domain.heatmap

import org.example.test.domain.common.OrderDuration
import org.example.test.domain.orderbook.OrderBookSample

// Determines how long liquidity has rested continuously at a given
// price row by walking an ordered sample history backward from a
// reference sample, stopping at the first prior sample where that row
// carried no volume on either side. Kept as its own type so
// HeatmapWindow's column-building logic never re-implements streak
// detection itself, and so a different resting-duration rule can be
// swapped in without touching the window at all.
class RestingLiquidityDurationTracker(private val grid: HeatmapPriceGrid) {

    // orderedSamples must be sorted ascending by timestampMillis.
    // referenceIndex is the position within orderedSamples of the sample
    // this bucket's volume was aggregated from.
    fun durationAt(
        bucketIndex: Int,
        referenceIndex: Int,
        orderedSamples: List<OrderBookSample>,
    ): OrderDuration {
        if (referenceIndex !in orderedSamples.indices) return OrderDuration.ZERO

        val referenceTimestampMillis = orderedSamples[referenceIndex].timestampMillis
        var streakStartMillis = referenceTimestampMillis

        for (index in referenceIndex downTo 0) {
            val sample = orderedSamples[index]
            if (!sampleHasVolumeAt(sample, bucketIndex)) break
            streakStartMillis = sample.timestampMillis
        }

        return OrderDuration.of(referenceTimestampMillis - streakStartMillis)
    }

    private fun sampleHasVolumeAt(sample: OrderBookSample, bucketIndex: Int): Boolean {
        val hasBid = sample.bids.any { grid.bucketIndexOf(it.price) == bucketIndex }
        val hasAsk = sample.asks.any { grid.bucketIndexOf(it.price) == bucketIndex }
        return hasBid || hasAsk
    }
}
