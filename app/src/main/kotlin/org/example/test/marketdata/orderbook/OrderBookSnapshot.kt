package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookLevel

// Immutable, presentation-ready view of one instrument's order book at a
// point in time. Bids are highest-first, asks lowest-first, so a screen
// renders directly off these lists with no further sorting.
data class OrderBookSnapshot(
    val instrumentId: String,
    val bids: List<OrderBookLevel>,
    val asks: List<OrderBookLevel>,
    val sequence: Long,
    val updatedAtMillis: Long,
) {
    val bestBid: OrderBookLevel? get() = bids.firstOrNull()
    val bestAsk: OrderBookLevel? get() = asks.firstOrNull()

    val spread: Double?
        get() {
            val bid = bestBid ?: return null
            val ask = bestAsk ?: return null
            return ask.price - bid.price
        }

    companion object {
        fun empty(instrumentId: String): OrderBookSnapshot = OrderBookSnapshot(
            instrumentId = instrumentId,
            bids = emptyList(),
            asks = emptyList(),
            sequence = 0L,
            updatedAtMillis = 0L,
        )
    }
}
