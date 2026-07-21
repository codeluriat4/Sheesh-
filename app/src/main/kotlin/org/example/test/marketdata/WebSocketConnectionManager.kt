package org.example.test.marketdata

import kotlinx.coroutines.CoroutineScope

// Public composition root for a resilient WebSocket connection: wires the
// transport, duplicate-connection guard, heartbeat and reconnect policy
// into one cohesive object. Callers depend only on WebSocketService; the
// wiring order is the single place that changes if the resilience policy
// changes, so no consumer of this class is ever affected.
class WebSocketConnectionManager(
    scope: CoroutineScope,
    url: String,
    heartbeatIntervalMillis: Long = 20_000L,
    heartbeatPayload: String = "ping",
    reconnectStrategy: ReconnectStrategy = ExponentialBackoffReconnectStrategy(),
    transportFactory: (String) -> WebSocketService = { endpoint -> OkHttpWebSocketService(url = endpoint) },
    private val transport: WebSocketService = ReconnectingWebSocketService(
        delegate = HeartbeatingWebSocketService(
            delegate = SingleFlightWebSocketService(
                delegate = transportFactory(url),
                scope = scope,
            ),
            heartbeat = IntervalHeartbeatScheduler(
                scope = scope,
                intervalMillis = heartbeatIntervalMillis,
                pingPayload = heartbeatPayload,
            ),
            scope = scope,
        ),
        strategy = reconnectStrategy,
        scope = scope,
    ),
) : WebSocketService by transport
