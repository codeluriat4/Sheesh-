package org.example.test.marketdata

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// Owns exactly one responsibility: guaranteeing at most one in-flight or
// active connection exists on the wrapped transport at any time, no matter
// how many callers invoke connect() concurrently. Uses an atomic
// compare-and-set so the guard itself is race-free across threads; no
// other class needs to reason about connection duplication.
class SingleFlightWebSocketService(
    private val delegate: WebSocketService,
    private val scope: CoroutineScope,
) : WebSocketService by delegate {

    private val connectionActive = AtomicBoolean(false)
    private var watching = false

    override fun connect() {
        watchLifecycle()
        if (connectionActive.compareAndSet(false, true)) {
            delegate.connect()
        }
    }

    override fun disconnect(code: Int, reason: String) {
        connectionActive.set(false)
        delegate.disconnect(code, reason)
    }

    private fun watchLifecycle() {
        if (watching) return
        watching = true
        delegate.state.onEach { current ->
            when (current) {
                is ConnectionState.Closed, is ConnectionState.Failed -> connectionActive.set(false)
                else -> Unit
            }
        }.launchIn(scope)
    }
}
