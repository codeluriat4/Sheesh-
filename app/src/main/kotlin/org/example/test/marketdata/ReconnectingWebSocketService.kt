package org.example.test.marketdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Adds automatic reconnection on top of any WebSocketService. The decision
// of *when* to retry lives in ReconnectStrategy; this class only reacts to
// state changes and never becomes aware of the underlying transport or the
// message protocol carried over it.
class ReconnectingWebSocketService(
    private val delegate: WebSocketService,
    private val strategy: ReconnectStrategy,
    private val scope: CoroutineScope,
) : WebSocketService by delegate {

    private var attempt = 0
    private var watching = false

    override fun connect() {
        watchLifecycle()
        delegate.connect()
    }

    override fun disconnect(code: Int, reason: String) {
        watching = false
        delegate.disconnect(code, reason)
    }

    private fun watchLifecycle() {
        if (watching) return
        watching = true
        delegate.state.onEach { current ->
            when (current) {
                is ConnectionState.Open -> {
                    attempt = 0
                    strategy.reset()
                }
                is ConnectionState.Failed, is ConnectionState.Closed -> scheduleReconnectIfWatching()
                else -> Unit
            }
        }.launchIn(scope)
    }

    private fun scheduleReconnectIfWatching() {
        if (!watching) return
        val delayMillis = strategy.nextDelayMillis(attempt)
        attempt += 1
        scope.launch {
            delay(delayMillis)
            if (watching) delegate.connect()
        }
    }
}
