package org.example.test.marketdata.bitget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.WebSocketConnectionManager
import org.example.test.marketdata.WebSocketService

// Public entry point: a persistent, self-reconnecting connection to
// Bitget's public market-data WebSocket, subscribed to one order-book
// channel (BTCUSDT perpetual by default). Composes the reusable
// transport/heartbeat/reconnect building blocks with Bitget's wire
// format; no other class needs to know both sides at once, and the same
// transport stack is reusable for any other Bitget or non-Bitget channel.
class BitgetOrderBookStreamService(
    scope: CoroutineScope,
    private val channel: BitgetChannel = BitgetChannel.btcUsdtPerpetualOrderBook(),
    private val transport: WebSocketService = defaultTransport(scope),
    private val parser: BitgetMessageParser = JsonBitgetMessageParser(),
) {
    private val workScope = scope
    private val mutableEvents = MutableSharedFlow<BitgetOrderBookEvent>(extraBufferCapacity = 256)

    val events: SharedFlow<BitgetOrderBookEvent> = mutableEvents.asSharedFlow()
    val connectionState: Flow<ConnectionState> get() = transport.state

    fun start() {
        transport.state
            .onEach { current -> if (current is ConnectionState.Open) subscribe() }
            .launchIn(workScope)

        transport.incomingText
            .onEach { rawText -> parser.parse(rawText)?.let { mutableEvents.emit(it) } }
            .launchIn(workScope)

        transport.connect()
    }

    fun stop() {
        transport.disconnect()
    }

    private fun subscribe() {
        val request = BitgetSubscriptionRequest(
            operation = BitgetOperation.SUBSCRIBE,
            channels = listOf(channel),
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
