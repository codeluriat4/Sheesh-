package org.example.test.domain.chart

// Directional classification of a candle body, resolved once here instead
// of being re-branched on open/close at every place a color or icon is needed.
sealed interface CandleTrend {
    data object Bullish : CandleTrend
    data object Bearish : CandleTrend
    data object Neutral : CandleTrend

    companion object {
        private val bySign: Map<Int, CandleTrend> = mapOf(
            1 to Bullish,
            -1 to Bearish,
            0 to Neutral,
        )

        fun from(open: Double, close: Double): CandleTrend =
            bySign.getValue(close.compareTo(open).coerceIn(-1, 1))
    }
}
