package org.example.test.marketdata

import kotlinx.coroutines.flow.Flow

// Transport-only contract for a persistent WebSocket connection. Nothing
// here knows about any exchange, message schema, or subscription protocol,
// so any implementation is reusable for any text-based WebSocket endpoint.
interface WebSocketService {
    val state: Flow<ConnectionState>
    val incomingText: Flow<String>

    fun connect()
    fun send(text: String): Boolean
    fun disconnect(code: Int = 1000, reason: String = "client_close")
}
