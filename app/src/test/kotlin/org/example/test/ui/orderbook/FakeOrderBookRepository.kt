package org.example.test.ui.orderbook

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.orderbook.OrderBookSnapshot
import org.example.test.marketdata.repository.OrderBookRepository

// Test double standing in for a real repository. Lets a test push
// snapshot/connection updates directly and records lifecycle calls so
// assertions can check the ViewModel drives start/stop without a real
// socket or reducer being involved.
class FakeOrderBookRepository(
    initialSnapshot: OrderBookSnapshot = OrderBookSnapshot.empty(instrumentId = "BTCUSDT"),
    initialConnectionState: ConnectionState = ConnectionState.Idle,
) : OrderBookRepository {
    private val mutableOrderBook = MutableStateFlow(initialSnapshot)
    private val mutableConnectionState = MutableStateFlow(initialConnectionState)

    val startInvocationCount = AtomicInteger(0)
    val stopInvocationCount = AtomicInteger(0)

    override fun observeOrderBook(): Flow<OrderBookSnapshot> = mutableOrderBook.asStateFlow()

    override fun observeConnectionState(): Flow<ConnectionState> = mutableConnectionState.asStateFlow()

    override fun start() {
        startInvocationCount.incrementAndGet()
    }

    override fun stop() {
        stopInvocationCount.incrementAndGet()
    }

    fun emitSnapshot(snapshot: OrderBookSnapshot) {
        mutableOrderBook.value = snapshot
    }

    fun emitConnectionState(connectionState: ConnectionState) {
        mutableConnectionState.value = connectionState
    }
}
