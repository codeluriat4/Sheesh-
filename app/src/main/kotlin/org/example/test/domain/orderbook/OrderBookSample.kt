package org.example.test.domain.orderbook

// A timestamped view of both sides of an order book, expressed purely in
// domain types. Nothing here exposes transport, wire format, or which
// exchange produced it, so heatmap bucketing never depends on how a
// sample was reconstructed.
data class OrderBookSample(
    val timestampMillis: Long,
    val bids: List<BidLevel>,
    val asks: List<AskLevel>,
) {
    init {
        require(timestampMillis >= 0L) { "timestampMillis cannot be negative" }
    }
}
