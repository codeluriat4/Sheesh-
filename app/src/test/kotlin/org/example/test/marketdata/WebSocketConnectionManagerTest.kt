package org.example.test.marketdata

import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WebSocketConnectionManagerTest {

    @Test
    fun `restores connectivity after a simulated interruption`() = runTest {
        val fake = FakeWebSocketService()
        val manager = WebSocketConnectionManager(
            scope = backgroundScope,
            url = "wss://example.test/stream",
            reconnectStrategy = ExponentialBackoffReconnectStrategy(baseDelayMillis = 1_000L, maxDelayMillis = 2_000L),
            transportFactory = { fake },
        )

        manager.connect()
        fake.simulateOpen()
        assertEquals(1, fake.connectInvocationCount.get())

        fake.simulateInterruption()
        runCurrent()
        advanceTimeBy(1_001L)
        runCurrent()

        assertEquals(2, fake.connectInvocationCount.get())
    }

    @Test
    fun `repeated connect calls never open duplicate active connections`() = runTest {
        val fake = FakeWebSocketService()
        val manager = WebSocketConnectionManager(
            scope = backgroundScope,
            url = "wss://example.test/stream",
            transportFactory = { fake },
        )

        repeat(5) { manager.connect() }

        assertEquals(1, fake.connectInvocationCount.get())
    }

    @Test
    fun `a later connect after interruption does not duplicate the reconnect attempt`() = runTest {
        val fake = FakeWebSocketService()
        val manager = WebSocketConnectionManager(
            scope = backgroundScope,
            url = "wss://example.test/stream",
            reconnectStrategy = ExponentialBackoffReconnectStrategy(baseDelayMillis = 1_000L, maxDelayMillis = 2_000L),
            transportFactory = { fake },
        )

        manager.connect()
        fake.simulateOpen()
        fake.simulateInterruption()
        runCurrent()

        // A caller retrying manually mid-backoff opens the replacement
        // connection itself; once it does, the scheduled automatic
        // reconnect must not race it into opening a second socket.
        manager.connect()
        assertEquals(2, fake.connectInvocationCount.get())

        advanceTimeBy(1_001L)
        runCurrent()

        assertEquals(2, fake.connectInvocationCount.get())
    }
}
