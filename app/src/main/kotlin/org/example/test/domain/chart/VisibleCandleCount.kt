package org.example.test.domain.chart

// The fixed number of candles a chart always shows, independent of
// timeframe or how much history actually exists. Kept as its own type so
// every consumer reads the same single source of truth instead of a
// scattered magic number.
object VisibleCandleCount {
    const val VALUE: Int = 100
}
