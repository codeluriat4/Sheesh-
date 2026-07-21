package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceAxisTicksTest {

    @Test
    fun `of spaces ticks evenly with the first at the low and the last at the high`() {
        val range = PriceRange(low = Price(100.0), high = Price(200.0))

        val ticks = PriceAxisTicks.of(range, count = 5).ticks

        assertEquals(5, ticks.size)
        assertEquals(100.0, ticks.first().price.value, 0.0001)
        assertEquals(0.0, ticks.first().fraction, 0.0001)
        assertEquals(200.0, ticks.last().price.value, 0.0001)
        assertEquals(1.0, ticks.last().fraction, 0.0001)
        assertEquals(150.0, ticks[2].price.value, 0.0001)
        assertEquals(0.5, ticks[2].fraction, 0.0001)
    }

    @Test
    fun `of produces a single repeated price for a zero-span range`() {
        val range = PriceRange(low = Price(100.0), high = Price(100.0))

        val ticks = PriceAxisTicks.of(range, count = 3).ticks

        assertTrue(ticks.all { it.price.value == 100.0 })
    }

    @Test
    fun `of rejects a tick count below 2`() {
        val range = PriceRange(low = Price(100.0), high = Price(200.0))

        assertTrue(runCatching { PriceAxisTicks.of(range, count = 1) }.isFailure)
    }

    @Test
    fun `of defaults to five ticks`() {
        val range = PriceRange(low = Price(100.0), high = Price(200.0))

        assertEquals(5, PriceAxisTicks.of(range).ticks.size)
    }
}
