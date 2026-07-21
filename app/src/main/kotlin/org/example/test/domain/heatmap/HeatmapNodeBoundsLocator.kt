package org.example.test.domain.heatmap

import org.example.test.domain.chart.VisibleCandleCount

// Resolves the fractional bounding box a HeatmapNode occupies within its
// HeatmapWindow: the inverse of HeatmapNodeLocator, which turns a tap
// location into a node. This type turns a node back into the location it
// was painted at, mirroring HeatmapOverlay's own drawing geometry exactly
// so anchoring UI to a selected node never depends on Compose's draw
// scope or duplicates that math itself.
object HeatmapNodeBoundsLocator {
    fun locate(window: HeatmapWindow, node: HeatmapNode): HeatmapNodeBounds? {
        val columnIndex = window.columns.indexOfFirst { column -> column.cells.any { it == node } }
        if (columnIndex < 0) return null

        val rowCount = window.grid.rowCount
        val rowIndex = window.grid.bucketIndexOf(node.price)
        val columnWidthFraction = 1f / VisibleCandleCount.VALUE
        val rowHeightFraction = 1f / rowCount

        return HeatmapNodeBounds(
            leftFraction = columnWidthFraction * columnIndex,
            topFraction = 1f - ((rowIndex + 1) * rowHeightFraction),
            widthFraction = columnWidthFraction * node.widthFraction(window.maxDuration),
            heightFraction = rowHeightFraction,
        )
    }
}
