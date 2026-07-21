package org.example.test.marketdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.bitget.BitgetChannel
import org.example.test.marketdata.bitget.BitgetOrderBookStreamService
import org.example.test.marketdata.orderbook.OrderBookReducer
import org.example.test.marketdata.orderbook.OrderBookSnapshot

// Composition point between the Bitget transport/parsing stack and the
// presentation layer: owns stream lifecycle and republishes reconstructed
// snapshots as one shared, replayed flow, so any number of collectors
// (screen, widget, preview) see the same live book without opening
// duplicate sockets or re-running reconstruction from scratch.
class BitgetOrderBookRepository(
    private val scope: CoroutineScope,
    private val channel: BitgetChannel = BitgetChannel.btcUsdtPerpetualOrderBook(),
    private val streamService: BitgetOrderBookStreamService = BitgetOrderBookStreamService(
        scope = scope,
        channel = channel,
    ),
    private val reducer: OrderBookReducer = OrderBookReducer(instrumentId = channel.instId),
    private val replaySubscriptionTimeoutMillis: Long = 5_000L,
) : OrderBookRepository {

    private val sharedOrderBook: Flow<OrderBookSnapshot> by lazy {
        reducer.reduce(streamService.events)
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = replaySubscriptionTimeoutMillis),
                replay = 1,
            )
    }

    private val sharedConnectionState: Flow<ConnectionState> by lazy {
        streamService.connectionState
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = replaySubscriptionTimeoutMillis),
                replay = 1,
            )
    }

    override fun observeOrderBook(): Flow<OrderBookSnapshot> = sharedOrderBook

    override fun observeConnectionState(): Flow<ConnectionState> = sharedConnectionState

    override fun start() = streamService.start()

    override fun stop() = streamService.stop()
}
