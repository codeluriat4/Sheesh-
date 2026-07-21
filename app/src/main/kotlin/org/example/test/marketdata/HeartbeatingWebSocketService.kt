package org.example.test.marketdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// Adds an application-level text heartbeat on top of any WebSocketService.
// Some servers require a literal keepalive payload rather than protocol
// level ping frames; this decorator owns only that single concern.
class HeartbeatingWebSocketService(
    private val delegate: WebSocketService,
    private val heartbeat: HeartbeatScheduler,
    private val scope: CoroutineScope,
) : WebSocketService by delegate {

    private var watching = false

    override fun connect() {
        watchLifecycle()
        delegate.connect()
    }

    override fun disconnect(code: Int, reason: String) {
        heartbeat.stop()
        delegate.disconnect(code, reason)
    }

    private fun watchLifecycle() {
        if (watching) return
        watching = true
        delegate.state.onEach { current ->
            when (current) {
                is ConnectionState.Open -> heartbeat.start(delegate::send)
                is ConnectionState.Closed, is ConnectionState.Failed -> heartbeat.stop()
                else -> Unit
            }
        }.launchIn(scope)
    }
}
