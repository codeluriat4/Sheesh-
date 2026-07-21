package org.example.test.ui.chart

import androidx.compose.ui.unit.IntSize
import org.example.test.domain.heatmap.HeatmapNodeBounds
import org.junit.Assert.assertEquals
import org.junit.Test

class HeatmapPopupAnchorTest {

    private val canvasSize = IntSize(400, 800)
    private val popupSize = IntSize(120, 80)

    @Test
    fun `anchors above and to the right of the node when there is room`() {
        val bounds = HeatmapNodeBounds(
            leftFraction = 0.5f,
            topFraction = 0.5f,
            widthFraction = 0.05f,
            heightFraction = 0.02f,
        )

        val offset = HeatmapPopupAnchor.offsetFor(bounds, canvasSize, popupSize)

        val expectedX = (0.55f * canvasSize.width) + 12f
        val expectedY = (0.5f * canvasSize.height) - popupSize.height - 12f
        assertEquals(expectedX.toInt(), offset.x)
        assertEquals(expectedY.toInt(), offset.y)
    }

    @Test
    fun `falls back to below the node when there is no room above`() {
        val bounds = HeatmapNodeBounds(
            leftFraction = 0.0f,
            topFraction = 0.0f,
            widthFraction = 0.05f,
            heightFraction = 0.02f,
        )

        val offset = HeatmapPopupAnchor.offsetFor(bounds, canvasSize, popupSize)

        val expectedY = (0.02f * canvasSize.height) + 12f
        assertEquals(expectedY.toInt(), offset.y)
    }

    @Test
    fun `clamps the horizontal offset so the popup never overflows the right edge`() {
        val bounds = HeatmapNodeBounds(
            leftFraction = 0.98f,
            topFraction = 0.5f,
            widthFraction = 0.02f,
            heightFraction = 0.02f,
        )

        val offset = HeatmapPopupAnchor.offsetFor(bounds, canvasSize, popupSize)

        assertEquals(canvasSize.width - popupSize.width, offset.x)
    }

    @Test
    fun `returns zero when the canvas has not been measured yet`() {
        val bounds = HeatmapNodeBounds(0f, 0f, 0.05f, 0.02f)

        val offset = HeatmapPopupAnchor.offsetFor(bounds, IntSize.Zero, popupSize)

        assertEquals(0, offset.x)
        assertEquals(0, offset.y)
    }
}
