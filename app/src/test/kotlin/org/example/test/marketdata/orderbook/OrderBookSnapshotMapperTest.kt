package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderBookSnapshotMapperTest {

    @Test
    fun `maps bids and asks into domain levels`() {
        val snapshot = OrderBookSnapshot(
            instrumentId = "BTCUSDT",
            bids = listOf(OrderBookLevel(price = 100.0, size = 2.0)),
            asks = listOf(OrderBookLevel(price = 101.0, size = 3.0)),
            sequence = 1L,
            updatedAtMillis = 5_000L,
        )

        val sample = snapshot.toDomainSample()

        assertEquals(5_000L, sample.timestampMillis)
        assertEquals(100.0, sample.bids.single().price.value, 0.0001)
        assertEquals(2.0, sample.bids.single().volume.value, 0.0001)
        assertEquals(101.0, sample.asks.single().price.value, 0.0001)
        assertEquals(3.0, sample.asks.single().volume.value, 0.0001)
    }

    @Test
    fun `drops non-positive priced levels rather than crashing`() {
        val snapshot = OrderBookSnapshot(
            instrumentId = "BTCUSDT",
            bids = listOf(OrderBookLevel(price = 0.0, size = 2.0)),
            asks = emptyList(),
            sequence = 1L,
            updatedAtMillis = 5_000L,
        )

        val sample = snapshot.toDomainSample()

        assertEquals(0, sample.bids.size)
    }
}
