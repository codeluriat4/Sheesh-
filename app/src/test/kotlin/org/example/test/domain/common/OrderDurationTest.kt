package org.example.test.domain.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderDurationTest {

    @Test
    fun `rejects negative values but allows zero`() {
        assertTrue(runCatching { OrderDuration(-1L) }.isFailure)
        assertTrue(OrderDuration(0L).isZero)
    }

    @Test
    fun `fraction of max duration scales proportionally`() {
        val duration = OrderDuration.of(5_000L)
        val maxDuration = OrderDuration.of(10_000L)

        assertEquals(0.5f, duration.fractionOf(maxDuration), 0.0001f)
    }

    @Test
    fun `fraction is clamped to a visible minimum instead of vanishing`() {
        val duration = OrderDuration.of(0L)
        val maxDuration = OrderDuration.of(10_000L)

        assertTrue(duration.fractionOf(maxDuration) > 0f)
    }

    @Test
    fun `draws full length when there is no reference duration yet`() {
        val duration = OrderDuration.of(3_000L)

        assertEquals(1f, duration.fractionOf(OrderDuration.ZERO), 0.0001f)
    }
}
