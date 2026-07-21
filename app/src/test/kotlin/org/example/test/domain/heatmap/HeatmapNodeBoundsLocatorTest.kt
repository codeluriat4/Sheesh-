package org.example.test.domain.heatmap

import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapNodeBoundsLocatorTest {

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
    fun `locates the fractional bounds of a node in the last column`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )
        val sample = OrderBookSample(
            timestampMillis = openTimeMillis,
            bids = listOf(BidLevel(Price(99.0), Volume(10.0))),
            asks = emptyList(),
        )
        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = listOf(sample))
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        val cell = heatmapWindow.columns[lastColumnIndex].cells.single()
        val rowIndex = heatmapWindow.grid.bucketIndexOf(cell.price)
        val rowCount = heatmapWindow.grid.rowCount

        val bounds = HeatmapNodeBoundsLocator.locate(window = heatmapWindow, node = cell)!!

        assertEquals(lastColumnIndex / VisibleCandleCount.VALUE.toFloat(), bounds.leftFraction, TOLERANCE)
        assertEquals(1f - ((rowIndex + 1) / rowCount.toFloat()), bounds.topFraction, TOLERANCE)
        assertEquals(1f / rowCount, bounds.heightFraction, TOLERANCE)
        assertTrue(bounds.widthFraction > 0f)
    }

    @Test
    fun `returns null when the node does not belong to any column in the window`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )
        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = emptyList())
        val foreignNode = HeatmapNode(
            timestampMillis = openTimeMillis,
            aggregatedVolume = org.example.test.domain.orderbook.AggregatedVolume.empty(Price(100.0)),
            intensity = 0.5,
        )

        val bounds = HeatmapNodeBoundsLocator.locate(window = heatmapWindow, node = foreignNode)

        assertNull(bounds)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
