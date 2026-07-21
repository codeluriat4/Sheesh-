package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookUpdate

// Maintained state of one instrument's order book, built by folding a
// sequence of snapshot/delta pushes. Confined to a single coroutine by its
// caller (see OrderBookReducer); not safe to share across threads.
internal class OrderBookAccumulator(private val instrumentId: String) {
    private val bids = OrderBookLedger(OrderBookSide.BID)
    private val asks = OrderBookLedger(OrderBookSide.ASK)
    private var sequence = 0L
    private var updatedAtMillis = 0L

    fun fold(update: OrderBookUpdate): OrderBookAccumulator {
        val strategy = OrderBookMergeStrategies.forAction(update.action)
        strategy.apply(bids, update.bids)
        strategy.apply(asks, update.asks)
        sequence = update.sequence
        updatedAtMillis = update.timestampMillis
        return this
    }

    fun toSnapshot(): OrderBookSnapshot = OrderBookSnapshot(
        instrumentId = instrumentId,
        bids = bids.snapshot(),
        asks = asks.snapshot(),
        sequence = sequence,
        updatedAtMillis = updatedAtMillis,
    )
}
