package org.example.test.marketdata

import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SingleFlightWebSocketServiceTest {

    @Test
    fun `concurrent connect calls open exactly one underlying connection`() = runTest {
        val fake = FakeWebSocketService()
        val guarded = SingleFlightWebSocketService(fake, backgroundScope)

        repeat(10) { guarded.connect() }

        assertEquals(1, fake.connectInvocationCount.get())
    }

    @Test
    fun `guard releases after failure so a later connect is allowed`() = runTest {
        val fake = FakeWebSocketService()
        val guarded = SingleFlightWebSocketService(fake, backgroundScope)

        guarded.connect()
        fake.simulateOpen()
        fake.simulateInterruption()
        runCurrent()
        guarded.connect()

        assertEquals(2, fake.connectInvocationCount.get())
    }

    @Test
    fun `guard releases after intentional disconnect so reconnect is allowed`() = runTest {
        val fake = FakeWebSocketService()
        val guarded = SingleFlightWebSocketService(fake, backgroundScope)

        guarded.connect()
        fake.simulateOpen()
        guarded.disconnect()
        guarded.connect()

        assertEquals(2, fake.connectInvocationCount.get())
    }
}
