package org.example.test.domain.heatmap

// The fixed number of price rows a heatmap column is always bucketed
// into, independent of how wide the underlying PriceRange happens to be.
// Kept as its own type so every consumer reads the same single source of
// truth instead of a scattered magic number, mirroring VisibleCandleCount.
object HeatmapRowCount {
    const val VALUE: Int = 48
}
