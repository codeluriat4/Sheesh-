package org.example.test.marketdata.bitget

// One push from the order-book channel: either the initial full snapshot
// or an incremental delta. Bids arrive highest-first, asks lowest-first,
// so best-price lookups never require external sorting.
data class OrderBookUpdate(
    val instId: String,
    val action: OrderBookAction,
    val bids: List<OrderBookLevel>,
    val asks: List<OrderBookLevel>,
    val sequence: Long,
    val checksum: Long,
    val timestampMillis: Long,
) {
    val bestBid: OrderBookLevel? get() = bids.firstOrNull()
    val bestAsk: OrderBookLevel? get() = asks.firstOrNull()
}
