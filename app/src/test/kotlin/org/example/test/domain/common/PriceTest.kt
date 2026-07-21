package org.example.test.domain.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceTest {

    @Test
    fun `rejects zero and negative values`() {
        assertTrue(runCatching { Price(0.0) }.isFailure)
        assertTrue(runCatching { Price(-1.0) }.isFailure)
    }

    @Test
    fun `arithmetic and ordering operate on the wrapped value`() {
        val low = Price(100.0)
        val high = Price(150.0)

        assertEquals(Price(250.0), low + high)
        assertEquals(Price(50.0), high - low)
        assertTrue(high > low)
    }
}
