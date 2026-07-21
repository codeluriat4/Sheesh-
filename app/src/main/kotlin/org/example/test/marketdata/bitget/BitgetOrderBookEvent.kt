package org.example.test.marketdata.bitget

// Everything the BTCUSDT perpetual order-book stream can hand back to a
// consumer, expressed as one closed hierarchy so callers use `when`
// instead of type checks or null checks.
sealed interface BitgetOrderBookEvent {
    data class BookUpdate(val update: OrderBookUpdate) : BitgetOrderBookEvent
    data class SubscriptionConfirmed(val channel: BitgetChannel) : BitgetOrderBookEvent
    data class ServerError(val code: Int?, val message: String) : BitgetOrderBookEvent
}
