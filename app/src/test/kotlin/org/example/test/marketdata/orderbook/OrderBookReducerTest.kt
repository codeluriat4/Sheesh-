package org.example.test.marketdata.orderbook

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.test.marketdata.bitget.BitgetOrderBookEvent
import org.example.test.marketdata.bitget.BitgetInstrumentType
import org.example.test.marketdata.bitget.BitgetChannel
import org.example.test.marketdata.bitget.OrderBookAction
import org.example.test.marketdata.bitget.OrderBookLevel
import org.example.test.marketdata.bitget.OrderBookUpdate
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderBookReducerTest {

    private val reducer = OrderBookReducer(instrumentId = "BTCUSDT")

    @Test
    fun `snapshot then delta yields a book with merged and removed levels`() = runTest {
        val snapshot = BitgetOrderBookEvent.BookUpdate(
            OrderBookUpdate(
                instId = "BTCUSDT",
                action = OrderBookAction.SNAPSHOT,
                bids = listOf(OrderBookLevel(27000.0, 2.0), OrderBookLevel(26999.5, 1.0)),
                asks = listOf(OrderBookLevel(27000.5, 3.0), OrderBookLevel(27001.0, 0.5)),
                sequence = 1L,
                checksum = 0L,
                timestampMillis = 1_000L,
            ),
        )
        val delta = BitgetOrderBookEvent.BookUpdate(
            OrderBookUpdate(
                instId = "BTCUSDT",
                action = OrderBookAction.DELTA,
                bids = listOf(OrderBookLevel(26999.5, 0.0), OrderBookLevel(27000.25, 4.0)),
                asks = listOf(OrderBookLevel(27000.5, 1.5)),
                sequence = 2L,
                checksum = 0L,
                timestampMillis = 2_000L,
            ),
        )

        val snapshots = reducer.reduce(flowOf(snapshot, delta)).toList()

        assertEquals(2, snapshots.size)
        val latest = snapshots.last()
        assertEquals(listOf(OrderBookLevel(27000.25, 4.0), OrderBookLevel(27000.0, 2.0)), latest.bids)
        assertEquals(listOf(OrderBookLevel(27000.5, 1.5), OrderBookLevel(27001.0, 0.5)), latest.asks)
        assertEquals(2L, latest.sequence)
        assertEquals(2_000L, latest.updatedAtMillis)
        assertEquals(OrderBookLevel(27000.25, 4.0), latest.bestBid)
        assertEquals(OrderBookLevel(27000.5, 1.5), latest.bestAsk)
    }

    @Test
    fun `non book-update events are ignored and never reach the snapshot stream`() = runTest {
        val confirmed = BitgetOrderBookEvent.SubscriptionConfirmed(
            BitgetChannel(BitgetInstrumentType.USDT_FUTURES, "books", "BTCUSDT"),
        )
        val error = BitgetOrderBookEvent.ServerError(code = 1, message = "boom")

        val snapshots = reducer.reduce(flowOf(confirmed, error)).toList()

        assertEquals(0, snapshots.size)
    }
}
