package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CandlestickWindowTest {

    private val timeframe = Timeframe.OneMinute
    private val nowMillis = 1_000_000_000L

    private fun candleAt(index: Int) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = timeframe,
        openTimeMillis = index * timeframe.durationMillis,
        open = Price(100.0 + index),
        high = Price(110.0 + index),
        low = Price(90.0 + index),
        close = Price(105.0 + index),
        volume = Volume(1.0),
    )

    @Test
    fun `a window always has exactly VisibleCandleCount slots`() {
        listOf(0, 1, 50, 100, 250).forEach { seriesSize ->
            val series = (0 until seriesSize).map(::candleAt)
            val window = CandlestickWindow.fromSeries(series, timeframe, nowMillis)

            assertEquals(VisibleCandleCount.VALUE, window.slots.size)
        }
    }

    @Test
    fun `an empty series produces an all-empty window`() {
        val window = CandlestickWindow.fromSeries(emptyList(), timeframe, nowMillis)

        assertTrue(window.slots.all { it is CandleSlot.Empty })
    }

    @Test
    fun `a partial series is left-padded with Empty slots`() {
        val series = (0 until 40).map(::candleAt)
        val window = CandlestickWindow.fromSeries(series, timeframe, nowMillis)

        val padding = window.slots.take(VisibleCandleCount.VALUE - 40)
        val filled = window.slots.drop(VisibleCandleCount.VALUE - 40)

        assertTrue(padding.all { it is CandleSlot.Empty })
        assertTrue(filled.all { it is CandleSlot.Filled })
        assertEquals(series, filled.map { (it as CandleSlot.Filled).candle })
    }

    @Test
    fun `a longer series is truncated to the most recent VisibleCandleCount candles`() {
        val series = (0 until 250).map(::candleAt)
        val window = CandlestickWindow.fromSeries(series, timeframe, nowMillis)

        val expected = series.takeLast(VisibleCandleCount.VALUE)
        assertEquals(expected, window.slots.map { (it as CandleSlot.Filled).candle })
    }

    @Test
    fun `padding steps backward from the first real candle by the timeframe duration`() {
        val series = (0 until 3).map(::candleAt)
        val window = CandlestickWindow.fromSeries(series, timeframe, nowMillis)

        val padding = window.slots.take(VisibleCandleCount.VALUE - 3)
        val expectedTimes = padding.indices.map { i ->
            candleAt(0).openTimeMillis - ((padding.size - i) * timeframe.durationMillis)
        }

        assertEquals(expectedTimes, padding.map { it.openTimeMillis })
    }

    @Test
    fun `windowing result is independent of the timeframe requested`() {
        Timeframe.ALL.forEach { tf ->
            val series = (0 until 10).map { i ->
                Candlestick(
                    instrumentId = "BTCUSDT",
                    timeframe = tf,
                    openTimeMillis = i * tf.durationMillis,
                    open = Price(100.0),
                    high = Price(110.0),
                    low = Price(90.0),
                    close = Price(105.0),
                    volume = Volume(1.0),
                )
            }
            val window = CandlestickWindow.fromSeries(series, tf, nowMillis)

            assertEquals(VisibleCandleCount.VALUE, window.slots.size)
            assertTrue(window.slots.all { it.timeframe == tf })
        }
    }

    @Test
    fun `rejects a slots list of the wrong size`() {
        assertTrue(
            runCatching {
                CandlestickWindow(timeframe = timeframe, slots = listOf(CandleSlot.Empty(0L, timeframe)))
            }.isFailure,
        )
    }
}
