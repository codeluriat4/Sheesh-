package org.example.test.domain.heatmap

import org.example.test.domain.chart.VisibleCandleCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapTapLocationTest {

    @Test
    fun `rejects fractions outside zero to one`() {
        assertTrue(runCatching { HeatmapTapLocation(xFraction = -0.1, yFraction = 0.5) }.isFailure)
        assertTrue(runCatching { HeatmapTapLocation(xFraction = 1.1, yFraction = 0.5) }.isFailure)
        assertTrue(runCatching { HeatmapTapLocation(xFraction = 0.5, yFraction = -0.1) }.isFailure)
        assertTrue(runCatching { HeatmapTapLocation(xFraction = 0.5, yFraction = 1.1) }.isFailure)
    }

    @Test
    fun `a tap at the left edge resolves to the first column`() {
        val tap = HeatmapTapLocation(xFraction = 0.0, yFraction = 0.5)

        assertEquals(0, tap.columnIndex)
    }

    @Test
    fun `a tap at the right edge stays within the last column instead of overflowing`() {
        val tap = HeatmapTapLocation(xFraction = 1.0, yFraction = 0.5)

        assertEquals(VisibleCandleCount.VALUE - 1, tap.columnIndex)
    }

    @Test
    fun `a tap halfway across the canvas resolves to the middle column`() {
        val tap = HeatmapTapLocation(xFraction = 0.5, yFraction = 0.5)

        assertEquals(VisibleCandleCount.VALUE / 2, tap.columnIndex)
    }

    @Test
    fun `column local fraction reports how far across its own column a tap fell`() {
        val columnWidth = 1.0 / VisibleCandleCount.VALUE
        val tap = HeatmapTapLocation(xFraction = columnWidth * 3.25, yFraction = 0.5)

        assertEquals(3, tap.columnIndex)
        assertEquals(0.25, tap.columnLocalXFraction, 0.0001)
    }

    @Test
    fun `a tap at the top of the canvas resolves to the highest row`() {
        val tap = HeatmapTapLocation(xFraction = 0.5, yFraction = 0.0)

        assertEquals(9, tap.rowIndex(rowCount = 10))
    }

    @Test
    fun `a tap at the bottom of the canvas resolves to row zero`() {
        val tap = HeatmapTapLocation(xFraction = 0.5, yFraction = 0.999)

        assertEquals(0, tap.rowIndex(rowCount = 10))
    }
}
