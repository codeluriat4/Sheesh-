package org.example.test.marketdata.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.marketdata.FakeWebSocketService
import org.example.test.marketdata.bitget.BitgetCandlestickStreamService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BitgetCandlestickSeriesRepositoryTest {

    @Test
    fun `observed series reflects a pushed candle frame for the requested timeframe`() = runTest {
        val fakeTransport = FakeWebSocketService()
        val streamService = BitgetCandlestickStreamService(scope = backgroundScope, transport = fakeTransport)
        val repository = BitgetCandlestickSeriesRepository(scope = backgroundScope, streamService = streamService)
        val firstSeries = CompletableDeferred<List<Candlestick>>()

        backgroundScope.launch { firstSeries.complete(repository.observeCandles(Timeframe.OneMinute).first()) }
        runCurrent()

        repository.start()
        runCurrent() // let the stream service's listener coroutines subscribe before events fire
        fakeTransport.simulateOpen()
        fakeTransport.simulateMessage(CANDLE_FRAME)
        runCurrent()

        val series = firstSeries.await()
        assertEquals(1, series.size)
        assertEquals(1_695_716_040_000L, series.single().openTimeMillis)
        assertEquals(27_005.25, series.single().close.value, 0.0001)
    }

    @Test
    fun `collecting a timeframe subscribes only that timeframe's channel`() = runTest {
        val fakeTransport = FakeWebSocketService()
        val streamService = BitgetCandlestickStreamService(scope = backgroundScope, transport = fakeTransport)
        val repository = BitgetCandlestickSeriesRepository(scope = backgroundScope, streamService = streamService)

        backgroundScope.launch { repository.observeCandles(Timeframe.FiveMinutes).first() }
        runCurrent()

        repository.start()
        fakeTransport.simulateOpen()
        runCurrent()

        assertTrue(fakeTransport.sentMessages.any { it.contains("\"candle5m\"") })
        assertTrue(fakeTransport.sentMessages.none { it.contains("\"candle1m\"") })
    }

    private companion object {
        val CANDLE_FRAME = """
            {
                "action": "update",
                "arg": {"instType": "USDT-FUTURES", "channel": "candle1m", "instId": "BTCUSDT"},
                "data": [
                    ["1695716040000", "27000.0", "27015.5", "26990.0", "27005.25", "12.5", "337565.125"]
                ]
            }
        """.trimIndent()
    }
}
