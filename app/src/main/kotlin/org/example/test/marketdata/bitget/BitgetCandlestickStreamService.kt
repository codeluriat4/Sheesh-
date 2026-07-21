package org.example.test.marketdata.bitget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.test.domain.chart.Timeframe
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.WebSocketConnectionManager
import org.example.test.marketdata.WebSocketService

// Public entry point: a persistent, self-reconnecting connection to
// Bitget's public market-data WebSocket, publishing one shared stream of
// candle events across every timeframe a caller has subscribed to. Every
// timeframe ever subscribed is re-subscribed automatically whenever the
// socket reopens, since the exchange forgets subscriptions across a
// dropped connection. Composes the same reusable transport/heartbeat/
// reconnect building blocks BitgetOrderBookStreamService does; only the
// wire format and multi-channel subscription bookkeeping differ.
class BitgetCandlestickStreamService(
    scope: CoroutineScope,
    private val instId: String = "BTCUSDT",
    private val transport: WebSocketService = defaultTransport(scope),
    private val parser: BitgetCandleMessageParser = JsonBitgetCandleMessageParser(),
) {
    private val workScope = scope
    private val mutableEvents = MutableSharedFlow<BitgetCandleEvent>(extraBufferCapacity = 256)
    private val subscribedTimeframes = mutableSetOf<Timeframe>()

    val events: SharedFlow<BitgetCandleEvent> = mutableEvents.asSharedFlow()
    val connectionState: Flow<ConnectionState> get() = transport.state

    fun start() {
        transport.state
            .onEach { current -> if (current is ConnectionState.Open) resubscribeAll() }
            .launchIn(workScope)

        transport.incomingText
            .onEach { rawText -> parser.parse(rawText, instId)?.let { mutableEvents.emit(it) } }
            .launchIn(workScope)

        transport.connect()
    }

    fun stop() {
        transport.disconnect()
    }

    // Recorded immediately so a reconnect that happens before this
    // timeframe's own subscribe request lands still resubscribes it; sent
    // immediately too so an already-open socket doesn't wait for the next
    // reconnect to start streaming it.
    fun subscribe(timeframe: Timeframe) {
        val isNewSubscription = subscribedTimeframes.add(timeframe)
        if (isNewSubscription) sendSubscription(timeframe)
    }

    private fun resubscribeAll() {
        subscribedTimeframes.forEach(::sendSubscription)
    }

    private fun sendSubscription(timeframe: Timeframe) {
        val request = BitgetSubscriptionRequest(
            operation = BitgetOperation.SUBSCRIBE,
            channels = listOf(BitgetChannel.btcUsdtPerpetualCandle(timeframe)),
        )
        transport.send(request.toJsonString())
    }

    companion object {
        private fun defaultTransport(scope: CoroutineScope): WebSocketService =
            WebSocketConnectionManager(
                scope = scope,
                url = BitgetEndpoint.PUBLIC_URL,
                heartbeatIntervalMillis = 20_000L,
                heartbeatPayload = "ping",
            )
    }
}
