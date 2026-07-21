package org.example.test.marketdata.repository

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.marketdata.bitget.BitgetCandleEvent
import org.example.test.marketdata.bitget.BitgetChannel
import org.example.test.marketdata.bitget.BitgetInstrumentType
import org.junit.Assert.assertEquals
import org.junit.Test

class CandlestickSeriesReducerTest {

    private val reducer = CandlestickSeriesReducer(timeframe = Timeframe.OneMinute)

    private fun candle(openTimeMillis: Long, timeframe: Timeframe = Timeframe.OneMinute) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = timeframe,
        openTimeMillis = openTimeMillis,
        open = Price(100.0),
        high = Price(101.0),
        low = Price(99.0),
        close = Price(100.5),
        volume = Volume(1.0),
    )

    @Test
    fun `folds matching-timeframe pushes into an ascending series`() = runTest {
        val first = BitgetCandleEvent.CandleUpdate(Timeframe.OneMinute, listOf(candle(1_000L)))
        val second = BitgetCandleEvent.CandleUpdate(Timeframe.OneMinute, listOf(candle(2_000L)))

        val series = reducer.reduce(flowOf(first, second)).toList()

        assertEquals(2, series.size)
        assertEquals(listOf(1_000L), series[0].map { it.openTimeMillis })
        assertEquals(listOf(1_000L, 2_000L), series[1].map { it.openTimeMillis })
    }

    @Test
    fun `pushes for a different timeframe are ignored`() = runTest {
        val wrongTimeframe = BitgetCandleEvent.CandleUpdate(
            Timeframe.FiveMinutes,
            listOf(candle(1_000L, Timeframe.FiveMinutes)),
        )

        val series = reducer.reduce(flowOf(wrongTimeframe)).toList()

        assertEquals(0, series.size)
    }

    @Test
    fun `non candle-update events never reach the series stream`() = runTest {
        val confirmed = BitgetCandleEvent.SubscriptionConfirmed(
            BitgetChannel(BitgetInstrumentType.USDT_FUTURES, "candle1m", "BTCUSDT"),
        )
        val error = BitgetCandleEvent.ServerError(code = 1, message = "boom")

        val series = reducer.reduce(flowOf(confirmed, error)).toList()

        assertEquals(0, series.size)
    }
}
