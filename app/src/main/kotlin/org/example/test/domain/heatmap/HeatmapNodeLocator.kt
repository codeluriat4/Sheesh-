package org.example.test.domain.heatmap

// Resolves a HeatmapTapLocation to the single HeatmapNode painted there,
// if any, mirroring HeatmapOverlay's own drawing geometry exactly: a
// node occupies its column from the column's left edge out to
// widthFraction of that column's own width, and whichever row its own
// price buckets into. Kept separate from HeatmapWindow itself so tap
// resolution can change independently of how a window's columns are
// built from history.
object HeatmapNodeLocator {
    fun locate(window: HeatmapWindow, tap: HeatmapTapLocation): HeatmapNode? {
        val column = window.columns.getOrNull(tap.columnIndex) ?: return null
        val rowIndex = tap.rowIndex(window.grid.rowCount)
        return column.cells.firstOrNull { cell ->
            window.grid.bucketIndexOf(cell.price) == rowIndex &&
                tap.columnLocalXFraction <= cell.widthFraction(window.maxDuration)
        }
    }
}
