package org.example.test.marketdata.repository

import kotlin.random.Random
import org.example.test.domain.chart.Timeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedCandlestickSeriesGeneratorTest {

    private val generator = SimulatedCandlestickSeriesGenerator(instrumentId = "BTCUSDT", random = Random(42))

    @Test
    fun `generates exactly the requested count`() {
        val series = generator.generate(timeframe = Timeframe.OneMinute, count = 100, endTimeMillis = 1_700_000_000_000L)

        assertEquals(100, series.size)
    }

    @Test
    fun `each candle sits in its own bucket, one timeframe duration apart`() {
        val series = generator.generate(timeframe = Timeframe.FiveMinutes, count = 10, endTimeMillis = 1_700_000_000_000L)

        val gaps = series.zipWithNext { a, b -> b.openTimeMillis - a.openTimeMillis }
        assertTrue(gaps.all { it == Timeframe.FiveMinutes.durationMillis })
    }

    @Test
    fun `every candle satisfies the OHLC ordering invariant`() {
        val series = generator.generate(timeframe = Timeframe.OneHour, count = 50, endTimeMillis = 1_700_000_000_000L)

        series.forEach { candle ->
            assertTrue(candle.high >= candle.open)
            assertTrue(candle.high >= candle.close)
            assertTrue(candle.low <= candle.open)
            assertTrue(candle.low <= candle.close)
        }
    }
}
