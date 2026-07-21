package org.example.test.domain.heatmap

import org.example.test.domain.chart.PriceRange
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Test

class RestingLiquidityDurationTrackerTest {

    private val grid = HeatmapPriceGrid(priceRange = PriceRange(low = Price(100.0), high = Price(200.0)), rowCount = 10)
    private val tracker = RestingLiquidityDurationTracker(grid)

    private fun sample(timestampMillis: Long, bidPrice: Double?) = OrderBookSample(
        timestampMillis = timestampMillis,
        bids = bidPrice?.let { listOf(BidLevel(Price(it), Volume(1.0))) } ?: emptyList(),
        asks = emptyList(),
    )

    @Test
    fun `measures duration back to the start of an unbroken streak`() {
        val samples = listOf(
            sample(0L, bidPrice = 150.0),
            sample(1_000L, bidPrice = 150.0),
            sample(2_000L, bidPrice = 150.0),
        )
        val bucketIndex = grid.bucketIndexOf(Price(150.0))

        val duration = tracker.durationAt(bucketIndex = bucketIndex, referenceIndex = 2, orderedSamples = samples)

        assertEquals(2_000L, duration.millis)
    }

    @Test
    fun `stops the streak at the sample where the bucket had no volume`() {
        val samples = listOf(
            sample(0L, bidPrice = null),
            sample(1_000L, bidPrice = 150.0),
            sample(2_000L, bidPrice = 150.0),
        )
        val bucketIndex = grid.bucketIndexOf(Price(150.0))

        val duration = tracker.durationAt(bucketIndex = bucketIndex, referenceIndex = 2, orderedSamples = samples)

        assertEquals(1_000L, duration.millis)
    }

    @Test
    fun `is zero for a bucket that just appeared`() {
        val samples = listOf(sample(0L, bidPrice = 150.0))
        val bucketIndex = grid.bucketIndexOf(Price(150.0))

        val duration = tracker.durationAt(bucketIndex = bucketIndex, referenceIndex = 0, orderedSamples = samples)

        assertEquals(0L, duration.millis)
    }
}
