package org.example.test.domain.chart

// A fixed-size, time-ordered view of a candle series: always exactly
// VisibleCandleCount.VALUE slots, regardless of timeframe or how much
// history the underlying series actually has. Building this shape from a
// raw series happens once, here, so no screen ever renders a
// variable-length list of candles.
data class CandlestickWindow(
    val timeframe: Timeframe,
    val slots: List<CandleSlot>,
) {
    init {
        require(slots.size == VisibleCandleCount.VALUE) {
            "a window must contain exactly ${VisibleCandleCount.VALUE} slots, had ${slots.size}"
        }
        require(slots.all { it.timeframe == timeframe }) { "every slot must share the window's timeframe" }
    }

    val priceRange: PriceRange
        get() = PriceRange.of(slots.mapNotNull { it.fold(onFilled = { f -> f.candle }, onEmpty = { null }) })

    companion object {
        // Selects the most recent VisibleCandleCount.VALUE candles from a
        // series ordered ascending by open time, then left-pads with Empty
        // slots stepping backwards by the timeframe's own duration when
        // fewer than that many candles exist yet. The result is always
        // exactly VisibleCandleCount.VALUE slots wide no matter the input
        // size or which timeframe it was built for.
        fun fromSeries(candles: List<Candlestick>, timeframe: Timeframe, nowMillis: Long): CandlestickWindow {
            val recent = candles.takeLast(VisibleCandleCount.VALUE)
            val missing = VisibleCandleCount.VALUE - recent.size

            val anchorOpenTimeMillis = recent.firstOrNull()?.openTimeMillis
                ?: timeframe.bucketStartMillis(nowMillis)

            val padding = (missing downTo 1).map { stepsBeforeAnchor ->
                CandleSlot.Empty(
                    openTimeMillis = anchorOpenTimeMillis - (stepsBeforeAnchor * timeframe.durationMillis),
                    timeframe = timeframe,
                )
            }

            val filled = recent.map { CandleSlot.Filled(it) }
            return CandlestickWindow(timeframe = timeframe, slots = padding + filled)
        }
    }
}
