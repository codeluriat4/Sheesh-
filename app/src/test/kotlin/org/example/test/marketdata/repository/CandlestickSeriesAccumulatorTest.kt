package org.example.test.marketdata.repository

import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Test

class CandlestickSeriesAccumulatorTest {

    private fun candle(openTimeMillis: Long, close: Double) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = Timeframe.OneMinute,
        openTimeMillis = openTimeMillis,
        open = Price(close),
        high = Price(close + 1.0),
        low = Price(close - 1.0),
        close = Price(close),
        volume = Volume(1.0),
    )

    @Test
    fun `toSeries is ascending by open time regardless of push order`() {
        val accumulator = CandlestickSeriesAccumulator(maxSize = 10)
            .fold(listOf(candle(2_000L, 20.0)))
            .fold(listOf(candle(1_000L, 10.0)))

        assertEquals(listOf(1_000L, 2_000L), accumulator.toSeries().map { it.openTimeMillis })
    }

    @Test
    fun `a push with an existing open time replaces the still-forming bar`() {
        val accumulator = CandlestickSeriesAccumulator(maxSize = 10)
            .fold(listOf(candle(1_000L, 10.0)))
            .fold(listOf(candle(1_000L, 12.0)))

        val series = accumulator.toSeries()
        assertEquals(1, series.size)
        assertEquals(12.0, series.single().close.value, 0.0001)
    }

    @Test
    fun `evicts the oldest bar once beyond maxSize`() {
        val accumulator = CandlestickSeriesAccumulator(maxSize = 2)
            .fold(listOf(candle(1_000L, 10.0)))
            .fold(listOf(candle(2_000L, 11.0)))
            .fold(listOf(candle(3_000L, 12.0)))

        assertEquals(listOf(2_000L, 3_000L), accumulator.toSeries().map { it.openTimeMillis })
    }
}
