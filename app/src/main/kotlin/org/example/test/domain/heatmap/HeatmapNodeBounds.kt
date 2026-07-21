package org.example.test.domain.heatmap

// The fractional bounding box, in the same [0,1] coordinate space as
// HeatmapTapLocation, that a single HeatmapNode occupies when painted.
// Kept as plain data next to the fractions it carries, so any consumer
// that anchors UI to a node just reads this rather than re-deriving
// pixel geometry itself.
data class HeatmapNodeBounds(
    val leftFraction: Float,
    val topFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
) {
    val centerXFraction: Float get() = leftFraction + widthFraction / 2f
    val centerYFraction: Float get() = topFraction + heightFraction / 2f
}
