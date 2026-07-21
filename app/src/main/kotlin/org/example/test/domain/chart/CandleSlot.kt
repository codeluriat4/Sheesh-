package org.example.test.domain.chart

// One position in a fixed-width chart window. A window always has exactly
// VisibleCandleCount.VALUE slots, but not every slot necessarily has
// traded data behind it yet (e.g. near the start of an instrument's
// history), so a slot is either Filled with a real candle or Empty at a
// known point in time.
sealed interface CandleSlot {
    val openTimeMillis: Long
    val timeframe: Timeframe

    // Double-dispatch instead of a when/is check at every call site: a
    // consumer that needs different behavior per slot type supplies both
    // branches once here, and the slot itself resolves which applies.
    fun <R> fold(onFilled: (Filled) -> R, onEmpty: (Empty) -> R): R

    data class Filled(val candle: Candlestick) : CandleSlot {
        override val openTimeMillis: Long get() = candle.openTimeMillis
        override val timeframe: Timeframe get() = candle.timeframe

        override fun <R> fold(onFilled: (Filled) -> R, onEmpty: (Empty) -> R): R = onFilled(this)
    }

    data class Empty(
        override val openTimeMillis: Long,
        override val timeframe: Timeframe,
    ) : CandleSlot {
        override fun <R> fold(onFilled: (Filled) -> R, onEmpty: (Empty) -> R): R = onEmpty(this)
    }
}
