package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CandlestickTest {

    private fun candle(open: Double, high: Double, low: Double, close: Double) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = Timeframe.OneMinute,
        openTimeMillis = 60_000L,
        open = Price(open),
        high = Price(high),
        low = Price(low),
        close = Price(close),
        volume = Volume(10.0),
    )

    @Test
    fun `close time is open time plus the timeframe duration`() {
        assertEquals(120_000L, candle(100.0, 110.0, 90.0, 105.0).closeTimeMillis)
    }

    @Test
    fun `trend reflects close relative to open`() {
        assertEquals(CandleTrend.Bullish, candle(100.0, 110.0, 90.0, 105.0).trend)
        assertEquals(CandleTrend.Bearish, candle(100.0, 110.0, 90.0, 95.0).trend)
        assertEquals(CandleTrend.Neutral, candle(100.0, 110.0, 90.0, 100.0).trend)
    }

    @Test
    fun `body and wick sizes are derived from the four prices`() {
        val bar = candle(open = 100.0, high = 120.0, low = 90.0, close = 110.0)

        assertEquals(10.0, bar.bodySize, 0.0001)
        assertEquals(30.0, bar.range, 0.0001)
        assertEquals(10.0, bar.upperWickSize, 0.0001)
        assertEquals(10.0, bar.lowerWickSize, 0.0001)
    }

    @Test
    fun `rejects a high that is not the largest price`() {
        assertTrue(runCatching { candle(open = 100.0, high = 95.0, low = 90.0, close = 92.0) }.isFailure)
    }

    @Test
    fun `rejects a low that is not the smallest price`() {
        assertTrue(runCatching { candle(open = 100.0, high = 110.0, low = 101.0, close = 105.0) }.isFailure)
    }
}
