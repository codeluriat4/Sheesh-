package org.example.test.domain.heatmap

// One vertical slice of the heatmap: every price-bucket cell captured at
// a single point in time. Columns are built one per CandleSlot at the
// same index, so an overlay renderer can align a column to a candle by
// position alone, never by re-deriving time from either side.
data class HeatmapColumn(
    val timestampMillis: Long,
    val cells: List<HeatmapNode>,
)
