package org.example.test.domain.heatmap

import org.example.test.domain.chart.PriceRange
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AskLevel
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Test

class HeatmapViewportTest {

    private val candleRange = PriceRange(low = Price(100.0), high = Price(200.0))

    @Test
    fun `keeps the candle range unchanged when no sample falls outside it`() {
        val samples = listOf(
            OrderBookSample(
                timestampMillis = 0L,
                bids = listOf(BidLevel(Price(150.0), Volume(1.0))),
                asks = listOf(AskLevel(Price(160.0), Volume(1.0))),
            ),
        )

        val scaled = HeatmapViewport.scaledRange(candleRange, samples)

        assertEquals(candleRange, scaled)
    }

    @Test
    fun `widens the low bound to enclose a resting bid below the candle range`() {
        val samples = listOf(
            OrderBookSample(
                timestampMillis = 0L,
                bids = listOf(BidLevel(Price(80.0), Volume(1.0))),
                asks = emptyList(),
            ),
        )

        val scaled = HeatmapViewport.scaledRange(candleRange, samples)

        assertEquals(Price(80.0), scaled.low)
        assertEquals(candleRange.high, scaled.high)
    }

    @Test
    fun `widens the high bound to enclose a resting ask above the candle range`() {
        val samples = listOf(
            OrderBookSample(
                timestampMillis = 0L,
                bids = emptyList(),
                asks = listOf(AskLevel(Price(250.0), Volume(1.0))),
            ),
        )

        val scaled = HeatmapViewport.scaledRange(candleRange, samples)

        assertEquals(candleRange.low, scaled.low)
        assertEquals(Price(250.0), scaled.high)
    }

    @Test
    fun `widens both bounds across multiple samples at once`() {
        val samples = listOf(
            OrderBookSample(timestampMillis = 0L, bids = listOf(BidLevel(Price(90.0), Volume(1.0))), asks = emptyList()),
            OrderBookSample(timestampMillis = 1_000L, bids = emptyList(), asks = listOf(AskLevel(Price(240.0), Volume(1.0)))),
        )

        val scaled = HeatmapViewport.scaledRange(candleRange, samples)

        assertEquals(Price(90.0), scaled.low)
        assertEquals(Price(240.0), scaled.high)
    }

    @Test
    fun `falls back to the candle range when there is no history yet`() {
        val scaled = HeatmapViewport.scaledRange(candleRange, emptyList())

        assertEquals(candleRange, scaled)
    }
}
