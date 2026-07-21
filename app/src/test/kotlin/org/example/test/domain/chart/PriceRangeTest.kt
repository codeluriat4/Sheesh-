package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceRangeTest {

    private fun candle(low: Double, high: Double) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = Timeframe.OneMinute,
        openTimeMillis = 60_000L,
        open = Price((low + high) / 2),
        high = Price(high),
        low = Price(low),
        close = Price((low + high) / 2),
        volume = Volume(1.0),
    )

    @Test
    fun `of spans the lowest low and highest high across candles`() {
        val range = PriceRange.of(listOf(candle(90.0, 110.0), candle(80.0, 105.0), candle(95.0, 120.0)))

        assertEquals(80.0, range.low.value, 0.0001)
        assertEquals(120.0, range.high.value, 0.0001)
    }

    @Test
    fun `of falls back to a degenerate range for an empty collection`() {
        val range = PriceRange.of(emptyList())

        assertEquals(range.low.value, range.high.value, 0.0001)
    }

    @Test
    fun `fractionOf places the low and high prices at 0 and 1`() {
        val range = PriceRange(low = Price(90.0), high = Price(110.0))

        assertEquals(0.0, range.fractionOf(Price(90.0)), 0.0001)
        assertEquals(1.0, range.fractionOf(Price(110.0)), 0.0001)
        assertEquals(0.5, range.fractionOf(Price(100.0)), 0.0001)
    }

    @Test
    fun `fractionOf falls back to the midpoint for a zero-span range`() {
        val range = PriceRange(low = Price(100.0), high = Price(100.0))

        assertEquals(0.5, range.fractionOf(Price(100.0)), 0.0001)
    }

    @Test
    fun `rejects a low greater than high`() {
        assertTrue(runCatching { PriceRange(low = Price(110.0), high = Price(90.0)) }.isFailure)
    }
}
