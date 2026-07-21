package org.example.test.marketdata

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

// Test double standing in for a real transport. Records every physical
// connect attempt so tests can assert on how many underlying sockets were
// actually opened, independent of how many times connect() was called, and
// exposes simulate* methods to drive lifecycle transitions a real network
// interruption would otherwise cause.
class FakeWebSocketService : WebSocketService {
    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private val mutableIncomingText = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val mutableSentMessages = mutableListOf<String>()

    val connectInvocationCount = AtomicInteger(0)
    val sentMessages: List<String> get() = mutableSentMessages

    override val state = mutableState.asStateFlow()
    override val incomingText = mutableIncomingText.asSharedFlow()

    override fun connect() {
        connectInvocationCount.incrementAndGet()
        mutableState.value = ConnectionState.Connecting
    }

    override fun send(text: String): Boolean {
        mutableSentMessages += text
        return mutableState.value is ConnectionState.Open
    }

    override fun disconnect(code: Int, reason: String) {
        mutableState.value = ConnectionState.Closed(code, reason)
    }

    fun simulateOpen() {
        mutableState.value = ConnectionState.Open
    }

    fun simulateInterruption(error: Throwable = RuntimeException("simulated network interruption")) {
        mutableState.value = ConnectionState.Failed(error)
    }

    fun simulateServerDrop(code: Int = 1006, reason: String = "simulated_drop") {
        mutableState.value = ConnectionState.Closed(code, reason)
    }

    fun simulateMessage(text: String) {
        mutableIncomingText.tryEmit(text)
    }
}
