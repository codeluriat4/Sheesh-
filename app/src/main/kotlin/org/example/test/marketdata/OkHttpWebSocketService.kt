package org.example.test.marketdata

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

// Owns exactly one responsibility: driving a single OkHttp WebSocket
// connection and republishing its lifecycle/messages as flows. It knows
// nothing about reconnection policy, heartbeats, or message schemas, so it
// is reusable for any text-based WebSocket endpoint, not only Bitget's.
class OkHttpWebSocketService(
    private val url: String,
    private val client: OkHttpClient = defaultClient(),
) : WebSocketService {

    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private val mutableIncomingText = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val state = mutableState.asStateFlow()
    override val incomingText = mutableIncomingText.asSharedFlow()

    @Volatile private var socket: WebSocket? = null

    override fun connect() {
        val current = mutableState.value
        if (current is ConnectionState.Connecting || current is ConnectionState.Open) return
        mutableState.value = ConnectionState.Connecting
        val request = Request.Builder().url(url).build()
        socket = client.newWebSocket(request, SocketListener())
    }

    override fun send(text: String): Boolean = socket?.send(text) ?: false

    override fun disconnect(code: Int, reason: String) {
        socket?.close(code, reason)
        socket = null
        mutableState.value = ConnectionState.Closed(code, reason)
    }

    private inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            mutableState.value = ConnectionState.Open
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            mutableIncomingText.tryEmit(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            mutableState.value = ConnectionState.Closing(code, reason)
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            mutableState.value = ConnectionState.Closed(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            mutableState.value = ConnectionState.Failed(t)
        }
    }

    companion object {
        // Protocol-level ping is disabled; Bitget expects an application
        // level text "ping" frame instead (see HeartbeatingWebSocketService).
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
