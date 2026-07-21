package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// Bid and ask volume aggregated into a single price bucket, e.g. for a
// depth chart or a heatmap column. Imbalance is derived here so no
// renderer re-implements the ratio math itself.
data class AggregatedVolume(
    val bucket: Price,
    val bidVolume: Volume,
    val askVolume: Volume,
) {
    val totalVolume: Volume get() = bidVolume + askVolume

    // Positive favors bids, negative favors asks; range is [-1, 1].
    val imbalance: Double
        get() {
            val total = totalVolume.value
            return if (total == 0.0) 0.0 else (bidVolume.value - askVolume.value) / total
        }

    companion object {
        fun empty(bucket: Price): AggregatedVolume = AggregatedVolume(bucket, Volume.ZERO, Volume.ZERO)
    }
}
