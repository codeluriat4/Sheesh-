package org.example.test.ui.orderbook

import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.orderbook.OrderBookSnapshot

// Single presentation-ready projection of the order book screen: the
// latest reconstructed snapshot paired with the socket's current
// lifecycle. The two are independent streams that a screen only ever
// needs to read together, so they live in one immutable value instead of
// forcing every collector to reconcile two separate observables itself.
data class OrderBookUiState(
    val snapshot: OrderBookSnapshot,
    val connectionState: ConnectionState,
) {
    companion object {
        // Value published before the first snapshot or connection event
        // arrives; never mutated in place, only ever replaced wholesale.
        fun initial(): OrderBookUiState = OrderBookUiState(
            snapshot = OrderBookSnapshot.empty(instrumentId = ""),
            connectionState = ConnectionState.Idle,
        )
    }
}
