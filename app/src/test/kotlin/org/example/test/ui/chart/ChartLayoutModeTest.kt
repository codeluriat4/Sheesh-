package org.example.test.ui.chart

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartLayoutModeTest {

    @Test
    fun `widths below the breakpoint select Compact`() {
        assertEquals(ChartLayoutMode.Compact, ChartLayoutMode.forWidth(0.dp))
        assertEquals(ChartLayoutMode.Compact, ChartLayoutMode.forWidth(599.dp))
    }

    @Test
    fun `widths at or above the breakpoint select Expanded`() {
        assertEquals(ChartLayoutMode.Expanded, ChartLayoutMode.forWidth(600.dp))
        assertEquals(ChartLayoutMode.Expanded, ChartLayoutMode.forWidth(1200.dp))
    }
}
