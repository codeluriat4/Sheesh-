package org.example.test.domain.heatmap

import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.common.OrderDuration
import org.example.test.domain.orderbook.AggregatedVolume
import org.example.test.domain.orderbook.OrderBookSample

// A fixed-size, time-aligned view of order book liquidity: exactly as
// many columns as the CandlestickWindow it overlays, all bucketed onto a
// price axis automatically widened, via HeatmapViewport, to enclose
// every level in the same history rather than the candle range alone.
// Building this shape from raw order book history happens once, here, so
// no renderer ever reconciles a heatmap's axes against the candle
// chart's axes itself, and no node is ever clipped by a candle range
// narrower than the liquidity being drawn.
data class HeatmapWindow(
    val grid: HeatmapPriceGrid,
    val columns: List<HeatmapColumn>,
    val maxDuration: OrderDuration = OrderDuration.ZERO,
) {
    init {
        require(columns.size == VisibleCandleCount.VALUE) {
            "a heatmap window must contain exactly ${VisibleCandleCount.VALUE} columns, had ${columns.size}"
        }
    }

    companion object {
        // Builds one column per slot of the given candlestick window, each
        // populated from the order book sample most recently observed
        // before that slot closes. A slot with no sample yet in history
        // simply paints no cells. Every cell's intensity is normalized
        // against the single largest bucket volume seen across the whole
        // window, so color intensity is comparable column to column
        // instead of being rescaled per slot. Every cell's resting
        // duration is likewise measured against the same ordered sample
        // history, so a node's width is comparable column to column the
        // same way its color already is.
        fun fromHistory(
            candlestickWindow: CandlestickWindow,
            samples: List<OrderBookSample>,
            rowCount: Int = HeatmapRowCount.VALUE,
            scale: HeatmapIntensityScale = LinearIntensityScale,
        ): HeatmapWindow {
            val orderedSamples = samples.sortedBy { it.timestampMillis }
            val viewportRange = HeatmapViewport.scaledRange(candlestickWindow.priceRange, orderedSamples)
            val grid = HeatmapPriceGrid(priceRange = viewportRange, rowCount = rowCount)
            val durationTracker = RestingLiquidityDurationTracker(grid)

            val aggregatedPerSlot = candlestickWindow.slots.map { slot ->
                val closeTimeMillis = slot.openTimeMillis + slot.timeframe.durationMillis
                val sampleIndex = orderedSamples.indexOfLast { it.timestampMillis < closeTimeMillis }
                val aggregatedVolumes = if (sampleIndex >= 0) grid.aggregate(orderedSamples[sampleIndex]) else emptyList()
                SlotAggregation(openTimeMillis = slot.openTimeMillis, sampleIndex = sampleIndex, aggregatedVolumes = aggregatedVolumes)
            }

            val maxVolume = aggregatedPerSlot
                .flatMap { it.aggregatedVolumes }
                .maxOfOrNull { it.totalVolume.value }
                ?: 0.0

            val columns = aggregatedPerSlot.map { slotAggregation ->
                HeatmapColumn(
                    timestampMillis = slotAggregation.openTimeMillis,
                    cells = slotAggregation.aggregatedVolumes.map { aggregated ->
                        val duration = durationTracker.durationAt(
                            bucketIndex = grid.bucketIndexOf(aggregated.bucket),
                            referenceIndex = slotAggregation.sampleIndex,
                            orderedSamples = orderedSamples,
                        )
                        HeatmapNode.from(
                            timestampMillis = slotAggregation.openTimeMillis,
                            aggregatedVolume = aggregated,
                            maxVolume = maxVolume,
                            duration = duration,
                            scale = scale,
                        )
                    },
                )
            }

            val maxDurationMillis = columns
                .flatMap { it.cells }
                .maxOfOrNull { it.duration.millis }
                ?: 0L

            return HeatmapWindow(grid = grid, columns = columns, maxDuration = OrderDuration.of(maxDurationMillis))
        }

        // One candlestick slot's resolved sample and the volumes bucketed
        // from it, kept together only for the two passes fromHistory needs
        // to make: one to find the window's own maxVolume, one to build
        // the actual nodes against it.
        private data class SlotAggregation(
            val openTimeMillis: Long,
            val sampleIndex: Int,
            val aggregatedVolumes: List<AggregatedVolume>,
        )
    }
}
