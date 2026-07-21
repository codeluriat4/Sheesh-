package org.example.test.marketdata

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconnectingWebSocketServiceTest {

    @Test
    fun `automatically reconnects after a simulated network interruption`() = runTest {
        val fake = FakeWebSocketService()
        val reconnecting = ReconnectingWebSocketService(
            delegate = fake,
            strategy = ExponentialBackoffReconnectStrategy(baseDelayMillis = 1_000L, maxDelayMillis = 4_000L),
            scope = backgroundScope,
        )

        reconnecting.connect()
        fake.simulateOpen()
        assertEquals(1, fake.connectInvocationCount.get())

        fake.simulateInterruption()
        runCurrent()
        advanceTimeBy(1_001L)
        runCurrent()

        assertEquals(2, fake.connectInvocationCount.get())

        fake.simulateOpen()
        assertTrue(fake.state.first() is ConnectionState.Open)
    }

    @Test
    fun `backs off further on repeated consecutive interruptions`() = runTest {
        val fake = FakeWebSocketService()
        val reconnecting = ReconnectingWebSocketService(
            delegate = fake,
            strategy = ExponentialBackoffReconnectStrategy(baseDelayMillis = 1_000L, maxDelayMillis = 8_000L),
            scope = backgroundScope,
        )

        reconnecting.connect()
        fake.simulateOpen()

        fake.simulateInterruption()
        runCurrent()
        advanceTimeBy(1_001L)
        runCurrent()
        assertEquals(2, fake.connectInvocationCount.get())

        fake.simulateInterruption()
        runCurrent()
        advanceTimeBy(1_500L)
        runCurrent()
        assertEquals(2, fake.connectInvocationCount.get())

        advanceTimeBy(600L)
        runCurrent()
        assertEquals(3, fake.connectInvocationCount.get())
    }

    @Test
    fun `does not reconnect after an intentional disconnect`() = runTest {
        val fake = FakeWebSocketService()
        val reconnecting = ReconnectingWebSocketService(
            delegate = fake,
            strategy = ExponentialBackoffReconnectStrategy(baseDelayMillis = 1_000L),
            scope = backgroundScope,
        )

        reconnecting.connect()
        fake.simulateOpen()
        reconnecting.disconnect()
        advanceTimeBy(10_000L)
        runCurrent()

        assertEquals(1, fake.connectInvocationCount.get())
    }
}
