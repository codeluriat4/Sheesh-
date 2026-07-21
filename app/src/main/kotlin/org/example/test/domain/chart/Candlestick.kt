package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// One immutable OHLCV bar for an instrument on a given timeframe. Every
// value derived from open/high/low/close/volume is computed here, next to
// the data it comes from, instead of being reconstructed by each screen.
data class Candlestick(
    val instrumentId: String,
    val timeframe: Timeframe,
    val openTimeMillis: Long,
    val open: Price,
    val high: Price,
    val low: Price,
    val close: Price,
    val volume: Volume,
) {
    init {
        require(instrumentId.isNotBlank()) { "instrumentId cannot be blank" }
        require(openTimeMillis >= 0L) { "openTimeMillis cannot be negative" }
        require(high >= open && high >= close && high >= low) { "high must be the largest of open/high/low/close" }
        require(low <= open && low <= close && low <= high) { "low must be the smallest of open/high/low/close" }
    }

    val closeTimeMillis: Long get() = openTimeMillis + timeframe.durationMillis

    val trend: CandleTrend get() = CandleTrend.from(open.value, close.value)

    val bodySize: Double get() = kotlin.math.abs(close.value - open.value)

    val range: Double get() = high.value - low.value

    val upperWickSize: Double get() = high.value - maxOf(open.value, close.value)

    val lowerWickSize: Double get() = minOf(open.value, close.value) - low.value
}
