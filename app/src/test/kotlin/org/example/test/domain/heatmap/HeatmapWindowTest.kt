package org.example.test.domain.heatmap

import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AskLevel
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapWindowTest {

    private val timeframe = Timeframe.OneMinute

    private fun candle(openTimeMillis: Long, price: Double) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = timeframe,
        openTimeMillis = openTimeMillis,
        open = Price(price),
        high = Price(price + 1.0),
        low = Price(price - 1.0),
        close = Price(price),
        volume = Volume(1.0),
    )

    @Test
    fun `produces exactly one column per candlestick slot`() {
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(0L, 100.0)),
            timeframe = timeframe,
            nowMillis = 0L,
        )

        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = emptyList())

        assertEquals(VisibleCandleCount.VALUE, heatmapWindow.columns.size)
    }

    @Test
    fun `populates a slot from the most recent sample before it closes`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )

        val sample = OrderBookSample(
            timestampMillis = openTimeMillis,
            bids = listOf(BidLevel(Price(99.0), Volume(10.0))),
            asks = listOf(AskLevel(Price(101.0), Volume(5.0))),
        )

        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = listOf(sample))
        val lastColumn = heatmapWindow.columns.last()

        assertTrue(lastColumn.cells.isNotEmpty())
        assertTrue(lastColumn.cells.all { it.intensity in 0.0..1.0 })
    }

    @Test
    fun `leaves a column empty when no sample precedes it`() {
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(0L, 100.0)),
            timeframe = timeframe,
            nowMillis = 0L,
        )

        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = emptyList())

        assertTrue(heatmapWindow.columns.all { it.cells.isEmpty() })
    }

    @Test
    fun `nodes carry a longer duration the longer their liquidity has rested`() {
        val openTimeMillis = 5_000L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )

        val restingBid = BidLevel(Price(99.0), Volume(10.0))
        val samples = listOf(
            OrderBookSample(timestampMillis = 3_000L, bids = listOf(restingBid), asks = emptyList()),
            OrderBookSample(timestampMillis = 4_000L, bids = listOf(restingBid), asks = emptyList()),
            OrderBookSample(timestampMillis = openTimeMillis, bids = listOf(restingBid), asks = emptyList()),
        )

        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = samples)
        val lastColumn = heatmapWindow.columns.last()

        assertTrue(lastColumn.cells.any { it.duration.millis > 0L })
        assertEquals(2_000L, heatmapWindow.maxDuration.millis)
    }

    @Test
    fun `a node resting outside the candle range keeps its own row instead of being clamped to an edge`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )

        // The candle range here spans roughly [99, 101]; this bid rests
        // far below it, at a price that would collapse into row 0 if the
        // heatmap's own price grid were never widened past the candles.
        val distantBid = BidLevel(Price(10.0), Volume(5.0))
        val sample = OrderBookSample(timestampMillis = openTimeMillis, bids = listOf(distantBid), asks = emptyList())

        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = listOf(sample))

        assertTrue(heatmapWindow.grid.priceRange.low <= Price(10.0))
        val distantCell = heatmapWindow.columns.last().cells.single()
        assertEquals(0, heatmapWindow.grid.bucketIndexOf(distantCell.price))
        assertTrue(distantCell.price.value in 9.0..11.0)
    }
}
