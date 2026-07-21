package org.example.test.domain.heatmap

import org.example.test.domain.chart.PriceRange
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AskLevel
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapPriceGridTest {

    private val grid = HeatmapPriceGrid(priceRange = PriceRange(low = Price(100.0), high = Price(200.0)), rowCount = 10)

    @Test
    fun `buckets a price into the row its value falls within`() {
        assertEquals(0, grid.bucketIndexOf(Price(100.0)))
        assertEquals(5, grid.bucketIndexOf(Price(150.0)))
        assertEquals(9, grid.bucketIndexOf(Price(199.9)))
    }

    @Test
    fun `clamps prices outside the range to the nearest edge row`() {
        assertEquals(0, grid.bucketIndexOf(Price(50.0)))
        assertEquals(9, grid.bucketIndexOf(Price(500.0)))
    }

    @Test
    fun `falls back to a single row when the range has zero span`() {
        val flatGrid = HeatmapPriceGrid(priceRange = PriceRange(low = Price(100.0), high = Price(100.0)), rowCount = 10)
        assertEquals(0, flatGrid.bucketIndexOf(Price(100.0)))
    }

    @Test
    fun `aggregates bid and ask levels landing in the same row`() {
        val sample = OrderBookSample(
            timestampMillis = 1_000L,
            bids = listOf(BidLevel(Price(150.0), Volume(2.0)), BidLevel(Price(151.0), Volume(3.0))),
            asks = listOf(AskLevel(Price(160.0), Volume(4.0))),
        )

        val aggregated = grid.aggregate(sample)

        val bidRow = aggregated.first { it.bidVolume.value > 0.0 }
        assertEquals(5.0, bidRow.bidVolume.value, 0.0001)

        val askRow = aggregated.first { it.askVolume.value > 0.0 }
        assertEquals(4.0, askRow.askVolume.value, 0.0001)
        assertTrue(aggregated.size <= 2)
    }
}
