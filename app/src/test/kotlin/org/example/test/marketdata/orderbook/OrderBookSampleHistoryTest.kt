package org.example.test.marketdata.orderbook

import org.example.test.domain.chart.Timeframe
import org.example.test.domain.orderbook.OrderBookSample
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderBookSampleHistoryTest {

    private fun sample(timestampMillis: Long) = OrderBookSample(
        timestampMillis = timestampMillis,
        bids = emptyList(),
        asks = emptyList(),
    )

    @Test
    fun `keeps only the latest sample per timeframe bucket`() {
        val history = OrderBookSampleHistory(maxBuckets = 10)
        val timeframe = Timeframe.OneMinute

        val first = history.record(sample(0L), timeframe)
        val second = history.record(sample(30_000L), timeframe)

        assertEquals(1, first.size)
        assertEquals(1, second.size)
        assertEquals(30_000L, second.single().timestampMillis)
    }

    @Test
    fun `evicts the oldest bucket once capacity is exceeded`() {
        val history = OrderBookSampleHistory(maxBuckets = 2)
        val timeframe = Timeframe.OneMinute

        history.record(sample(0L), timeframe)
        history.record(sample(60_000L), timeframe)
        val latest = history.record(sample(120_000L), timeframe)

        assertEquals(2, latest.size)
        assertEquals(60_000L, latest.first().timestampMillis)
        assertEquals(120_000L, latest.last().timestampMillis)
    }
}
