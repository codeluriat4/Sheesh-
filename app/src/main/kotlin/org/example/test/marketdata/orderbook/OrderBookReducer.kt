package org.example.test.marketdata.orderbook

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import org.example.test.marketdata.bitget.BitgetOrderBookEvent

// Owns exactly one responsibility: folding a stream of raw exchange events
// into a stream of coherent, presentation-ready order book snapshots. No
// other class needs to know how snapshot/delta pushes combine into state.
class OrderBookReducer(private val instrumentId: String) {
    fun reduce(events: Flow<BitgetOrderBookEvent>): Flow<OrderBookSnapshot> =
        events
            .filterIsInstance<BitgetOrderBookEvent.BookUpdate>()
            .scan(OrderBookAccumulator(instrumentId)) { accumulator, event -> accumulator.fold(event.update) }
            .drop(1)
            .map { it.toSnapshot() }
}
