package org.example.test.marketdata.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.FakeWebSocketService
import org.example.test.marketdata.bitget.BitgetOrderBookStreamService
import org.example.test.marketdata.orderbook.OrderBookSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class BitgetOrderBookRepositoryTest {

    @Test
    fun `observed order book reflects a pushed snapshot frame`() = runTest {
        val fakeTransport = FakeWebSocketService()
        val streamService = BitgetOrderBookStreamService(scope = backgroundScope, transport = fakeTransport)
        val repository = BitgetOrderBookRepository(scope = backgroundScope, streamService = streamService)
        val firstSnapshot = CompletableDeferred<OrderBookSnapshot>()

        backgroundScope.launch { firstSnapshot.complete(repository.observeOrderBook().first()) }
        runCurrent()

        repository.start()
        runCurrent()

        fakeTransport.simulateOpen()
        fakeTransport.simulateMessage(SNAPSHOT_FRAME)
        runCurrent()

        val snapshot = firstSnapshot.await()
        assertEquals("BTCUSDT", snapshot.instrumentId)
        assertEquals(27000.0, snapshot.bestBid?.price)
        assertEquals(27000.5, snapshot.bestAsk?.price)
        assertEquals(123L, snapshot.sequence)
    }

    @Test
    fun `observed connection state reflects the transport lifecycle`() = runTest {
        val fakeTransport = FakeWebSocketService()
        val streamService = BitgetOrderBookStreamService(scope = backgroundScope, transport = fakeTransport)
        val repository = BitgetOrderBookRepository(scope = backgroundScope, streamService = streamService)
        val firstState = CompletableDeferred<ConnectionState>()

        backgroundScope.launch { firstState.complete(repository.observeConnectionState().first { it is ConnectionState.Open }) }
        runCurrent()

        repository.start()
        fakeTransport.simulateOpen()
        runCurrent()

        assertEquals(ConnectionState.Open, firstState.await())
    }

    private companion object {
        val SNAPSHOT_FRAME = """
            {
                "action": "snapshot",
                "arg": {"instType": "USDT-FUTURES", "channel": "books", "instId": "BTCUSDT"},
                "data": [{
                    "asks": [["27000.5", "8.760"], ["27001.0", "0.400"]],
                    "bids": [["27000.0", "2.710"], ["26999.5", "1.460"]],
                    "checksum": 0,
                    "seq": 123,
                    "ts": "1695716059516"
                }],
                "ts": 1695716059516
            }
        """.trimIndent()
    }
}
