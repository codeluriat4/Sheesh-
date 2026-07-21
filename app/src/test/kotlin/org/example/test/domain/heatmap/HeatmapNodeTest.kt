package org.example.test.domain.heatmap

import org.example.test.domain.common.OrderDuration
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AggregatedVolume
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapNodeTest {

    @Test
    fun `linear scale normalizes volume against the maximum`() {
        val aggregated = AggregatedVolume(Price(100.0), bidVolume = Volume(5.0), askVolume = Volume(5.0))

        val node = HeatmapNode.from(
            timestampMillis = 1_000L,
            aggregatedVolume = aggregated,
            maxVolume = 20.0,
            scale = LinearIntensityScale,
        )

        assertEquals(0.5, node.intensity, 0.0001)
        assertEquals(Price(100.0), node.price)
    }

    @Test
    fun `logarithmic scale compresses large volumes toward one`() {
        val aggregated = AggregatedVolume(Price(100.0), bidVolume = Volume(90.0), askVolume = Volume(0.0))

        val node = HeatmapNode.from(
            timestampMillis = 1_000L,
            aggregatedVolume = aggregated,
            maxVolume = 100.0,
            scale = LogarithmicIntensityScale,
        )

        assertTrue(node.intensity in 0.0..1.0)
    }

    @Test
    fun `rejects intensity outside zero to one`() {
        val aggregated = AggregatedVolume.empty(Price(100.0))

        assertTrue(
            runCatching {
                HeatmapNode(timestampMillis = 0L, aggregatedVolume = aggregated, intensity = 1.5)
            }.isFailure,
        )
    }

    @Test
    fun `width fraction reflects this node's own duration`() {
        val aggregated = AggregatedVolume.empty(Price(100.0))
        val node = HeatmapNode(
            timestampMillis = 0L,
            aggregatedVolume = aggregated,
            intensity = 0.0,
            duration = OrderDuration.of(2_500L),
        )

        assertEquals(0.25f, node.widthFraction(OrderDuration.of(10_000L)), 0.0001f)
    }
}
