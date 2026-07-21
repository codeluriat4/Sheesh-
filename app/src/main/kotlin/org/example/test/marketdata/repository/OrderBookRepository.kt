package org.example.test.marketdata.repository

import kotlinx.coroutines.flow.Flow
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.orderbook.OrderBookSnapshot

// Presentation-facing contract: live order book state and connection
// health as observable streams. Nothing here exposes transport, wire
// format, or exchange-specific types, so a ViewModel or composable can
// depend on this without knowing which exchange or protocol is behind it.
interface OrderBookRepository {
    fun observeOrderBook(): Flow<OrderBookSnapshot>
    fun observeConnectionState(): Flow<ConnectionState>
    fun start()
    fun stop()
}
