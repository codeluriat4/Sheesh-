package org.example.test.ui.chart

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.example.test.domain.heatmap.HeatmapNodeBounds

// Resolves where a HeatmapNodeInfoPopup should sit relative to the
// selected node's own fractional bounds: anchored just above and to the
// right of the node when there is room, falling back to just below it,
// and always clamped so no edge is pushed outside the canvas it is
// anchored within, regardless of where on the chart the node was
// tapped. Kept as a pure function of sizes rather than a Composable, so
// the clamping behavior can change independently of how HeatmapChartHost
// lays out its own children.
object HeatmapPopupAnchor {
    fun offsetFor(nodeBounds: HeatmapNodeBounds, canvasSize: IntSize, popupSize: IntSize): IntOffset {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return IntOffset.Zero

        val nodeRight = (nodeBounds.leftFraction + nodeBounds.widthFraction) * canvasSize.width
        val nodeTop = nodeBounds.topFraction * canvasSize.height
        val nodeBottom = (nodeBounds.topFraction + nodeBounds.heightFraction) * canvasSize.height

        val maxX = (canvasSize.width - popupSize.width).coerceAtLeast(0).toFloat()
        val maxY = (canvasSize.height - popupSize.height).coerceAtLeast(0).toFloat()

        val x = (nodeRight + ANCHOR_GAP_PX).coerceIn(0f, maxX)
        val preferredAboveY = nodeTop - popupSize.height - ANCHOR_GAP_PX
        val y = if (preferredAboveY >= 0f) preferredAboveY else (nodeBottom + ANCHOR_GAP_PX).coerceIn(0f, maxY)

        return IntOffset(x.toInt(), y.toInt())
    }

    private const val ANCHOR_GAP_PX = 12f
}
