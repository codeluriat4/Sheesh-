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
import org.junit.Assert.assertNull
import org.junit.Test

class HeatmapNodeLocatorTest {

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

    // Centers a tap on the given column/row pair using the same
    // column/row math HeatmapTapLocation itself resolves against, so
    // tests exercise real hit resolution rather than assuming an offset.
    private fun tapAt(columnIndex: Int, rowIndex: Int, rowCount: Int): HeatmapTapLocation {
        val xFraction = (columnIndex + 0.5) / VisibleCandleCount.VALUE
        val rowFromTop = rowCount - 1 - rowIndex
        val yFraction = (rowFromTop + 0.5) / rowCount
        return HeatmapTapLocation(xFraction = xFraction, yFraction = yFraction)
    }

    @Test
    fun `resolves the node painted at the tapped column and row`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )
        val sample = OrderBookSample(
            timestampMillis = openTimeMillis,
            bids = listOf(BidLevel(Price(99.0), Volume(10.0))),
            asks = listOf(AskLevel(Price(100.5), Volume(5.0))),
        )
        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = listOf(sample))
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        // Two cells land in this column, one per row the bid and ask
        // levels bucket into; the bid's bucket is the lower-priced one.
        val cell = heatmapWindow.columns[lastColumnIndex].cells.minByOrNull { it.price.value }!!
        val rowIndex = heatmapWindow.grid.bucketIndexOf(cell.price)

        val located = HeatmapNodeLocator.locate(
            window = heatmapWindow,
            tap = tapAt(columnIndex = lastColumnIndex, rowIndex = rowIndex, rowCount = heatmapWindow.grid.rowCount),
        )

        assertEquals(cell, located)
    }

    @Test
    fun `returns null when the tapped column has no cells`() {
        val openTimeMillis = 0L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )
        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = emptyList())

        val located = HeatmapNodeLocator.locate(
            window = heatmapWindow,
            tap = tapAt(columnIndex = 0, rowIndex = 0, rowCount = heatmapWindow.grid.rowCount),
        )

        assertNull(located)
    }

    @Test
    fun `returns null when the tapped row has a cell but the tap falls beyond its drawn width`() {
        val openTimeMillis = 5_000L
        val window = CandlestickWindow.fromSeries(
            candles = listOf(candle(openTimeMillis, 100.0)),
            timeframe = timeframe,
            nowMillis = openTimeMillis,
        )
        val restingBid = BidLevel(Price(99.0), Volume(10.0))
        val freshBid = BidLevel(Price(100.5), Volume(10.0))
        val samples = listOf(
            OrderBookSample(timestampMillis = 3_000L, bids = listOf(restingBid), asks = emptyList()),
            OrderBookSample(timestampMillis = 4_000L, bids = listOf(restingBid), asks = emptyList()),
            OrderBookSample(
                timestampMillis = openTimeMillis,
                bids = listOf(restingBid, freshBid),
                asks = emptyList(),
            ),
        )
        val heatmapWindow = HeatmapWindow.fromHistory(candlestickWindow = window, samples = samples)
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        val freshCell = heatmapWindow.columns[lastColumnIndex].cells.first { it.duration.millis == 0L }
        val rowIndex = heatmapWindow.grid.bucketIndexOf(freshCell.price)

        // The fresh cell is drawn at its minimum visible width fraction,
        // far short of a full column; a tap centered on the column
        // instead of hugging its left edge should miss it entirely.
        val tap = HeatmapTapLocation(xFraction = (lastColumnIndex + 0.9) / VisibleCandleCount.VALUE, yFraction = tapAt(lastColumnIndex, rowIndex, heatmapWindow.grid.rowCount).yFraction)

        val located = HeatmapNodeLocator.locate(window = heatmapWindow, tap = tap)

        assertNull(located)
    }

    @Test
    fun `a tap past the right edge still resolves against the last column`() {
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
        val cell = heatmapWindow.columns.last().cells.single()
        val rowIndex = heatmapWindow.grid.bucketIndexOf(cell.price)
        val rowFromTop = heatmapWindow.grid.rowCount - 1 - rowIndex
        val yFraction = (rowFromTop + 0.5) / heatmapWindow.grid.rowCount

        val located = HeatmapNodeLocator.locate(
            window = heatmapWindow,
            tap = HeatmapTapLocation(xFraction = 1.0, yFraction = yFraction),
        )

        assertEquals(cell, located)
    }
}
