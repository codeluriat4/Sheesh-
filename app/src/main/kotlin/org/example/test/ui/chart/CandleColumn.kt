package org.example.test.ui.chart

// The horizontal slot and vertical drawing bounds a single candle glyph
// paints itself within. Computed once per slot instead of being
// re-derived inline inside every draw call.
data class CandleColumn(
    val centerX: Float,
    val bodyWidth: Float,
    val topY: Float,
    val bottomY: Float,
)
