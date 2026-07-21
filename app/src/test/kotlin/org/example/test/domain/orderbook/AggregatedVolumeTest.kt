package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Test

class AggregatedVolumeTest {

    @Test
    fun `total volume sums both sides`() {
        val bucket = AggregatedVolume(Price(100.0), bidVolume = Volume(3.0), askVolume = Volume(1.0))

        assertEquals(Volume(4.0), bucket.totalVolume)
    }

    @Test
    fun `imbalance favors the heavier side and is zero when empty`() {
        val bidHeavy = AggregatedVolume(Price(100.0), bidVolume = Volume(3.0), askVolume = Volume(1.0))
        val empty = AggregatedVolume.empty(Price(100.0))

        assertEquals(0.5, bidHeavy.imbalance, 0.0001)
        assertEquals(0.0, empty.imbalance, 0.0001)
    }
}
