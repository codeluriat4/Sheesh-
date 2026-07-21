package org.example.test.domain.heatmap

import org.example.test.domain.chart.VisibleCandleCount

// A single tap point expressed as fractional coordinates within a
// heatmap's own canvas: 0.0 at the left/top edge, 1.0 at the right/
// bottom edge. Kept fraction-based rather than pixel-based so hit
// resolution never depends on how large the canvas happened to be
// measured, mirroring how HeatmapOverlay itself paints purely from
// fractions of size.width/size.height.
data class HeatmapTapLocation(val xFraction: Double, val yFraction: Double) {
    init {
        require(xFraction in 0.0..1.0) { "xFraction must be within [0,1], was $xFraction" }
        require(yFraction in 0.0..1.0) { "yFraction must be within [0,1], was $yFraction" }
    }

    // Which column this tap falls into, aligned 1:1 with
    // CandlestickChart's own column count.
    val columnIndex: Int
        get() = (xFraction * VisibleCandleCount.VALUE).toInt().coerceIn(0, VisibleCandleCount.VALUE - 1)

    // How far across its own column this tap falls, as a fraction of
    // that column's width; compared against a node's own widthFraction
    // rather than the column's full width, since a node is not always
    // drawn as wide as its column.
    val columnLocalXFraction: Double
        get() = (xFraction * VisibleCandleCount.VALUE) - columnIndex

    // Which price row this tap falls into. Row 0 is the bottom-most row,
    // mirroring HeatmapOverlay's own top = size.height - (rowIndex+1) *
    // rowHeight.
    fun rowIndex(rowCount: Int): Int {
        val rowFromTop = (yFraction * rowCount).toInt().coerceIn(0, rowCount - 1)
        return rowCount - 1 - rowFromTop
    }
}
