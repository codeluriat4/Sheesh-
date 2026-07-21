package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderBookLevelTest {

    @Test
    fun `higher priced bid is better than a lower priced bid`() {
        val higher = BidLevel(Price(101.0), Volume(1.0))
        val lower = BidLevel(Price(100.0), Volume(1.0))

        assertTrue(higher.isBetterThan(lower))
        assertFalse(lower.isBetterThan(higher))
    }

    @Test
    fun `lower priced ask is better than a higher priced ask`() {
        val lower = AskLevel(Price(100.0), Volume(1.0))
        val higher = AskLevel(Price(101.0), Volume(1.0))

        assertTrue(lower.isBetterThan(higher))
        assertFalse(higher.isBetterThan(lower))
    }

    @Test
    fun `notional is price times volume`() {
        val level = BidLevel(Price(100.0), Volume(2.5))

        assertEquals(250.0, level.notional, 0.0001)
    }
}
