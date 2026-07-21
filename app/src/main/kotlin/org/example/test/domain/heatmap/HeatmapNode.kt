package org.example.test.domain.heatmap

import org.example.test.domain.common.OrderDuration
import org.example.test.domain.common.Price
import org.example.test.domain.orderbook.AggregatedVolume

// One cell of a price/time liquidity heatmap: a price bucket at a moment
// in time, with volume already normalized into a paintable intensity and
// its own resting duration carried alongside it, so a renderer never
// needs a second lookup to size what it is already drawing.
data class HeatmapNode(
    val timestampMillis: Long,
    val aggregatedVolume: AggregatedVolume,
    val intensity: Double,
    val duration: OrderDuration = OrderDuration.ZERO,
) {
    init {
        require(intensity in 0.0..1.0) { "intensity must be within [0,1], was $intensity" }
    }

    val price: Price get() = aggregatedVolume.bucket

    // This node's horizontal length as a fraction of its column's full
    // width, directly proportional to how long the order it represents
    // has rested, relative to the longest resting duration in the same
    // window.
    fun widthFraction(maxDuration: OrderDuration): Float = duration.fractionOf(maxDuration)

    companion object {
        fun from(
            timestampMillis: Long,
            aggregatedVolume: AggregatedVolume,
            maxVolume: Double,
            duration: OrderDuration = OrderDuration.ZERO,
            scale: HeatmapIntensityScale = LinearIntensityScale,
        ): HeatmapNode = HeatmapNode(
            timestampMillis = timestampMillis,
            aggregatedVolume = aggregatedVolume,
            intensity = scale.normalize(aggregatedVolume.totalVolume.value, maxVolume),
            duration = duration,
        )
    }
}
