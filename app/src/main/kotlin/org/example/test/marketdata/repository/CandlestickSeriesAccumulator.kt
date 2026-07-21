package org.example.test.marketdata.repository

import org.example.test.domain.chart.Candlestick

// Maintained ascending-by-open-time series for one timeframe, built by
// folding a sequence of candle pushes. A push whose open time matches an
// already-stored candle replaces it in place — this is how a
// still-forming bar's OHLCV keeps updating tick to tick; any other open
// time is stored as a newly closed bar. Bounded to the most recent
// maxSize bars, since nothing downstream ever windows further back than
// that. Confined to a single coroutine by its caller (see
// CandlestickSeriesReducer); not safe to share across threads.
internal class CandlestickSeriesAccumulator(private val maxSize: Int) {
    private val candlesByOpenTime = LinkedHashMap<Long, Candlestick>()

    fun fold(candles: List<Candlestick>): CandlestickSeriesAccumulator {
        candles.forEach { candle -> candlesByOpenTime[candle.openTimeMillis] = candle }
        evictOldestBeyondCapacity()
        return this
    }

    fun toSeries(): List<Candlestick> = candlesByOpenTime.values.sortedBy { it.openTimeMillis }

    private fun evictOldestBeyondCapacity() {
        while (candlesByOpenTime.size > maxSize) {
            val oldestOpenTime = candlesByOpenTime.keys.minOrNull() ?: return
            candlesByOpenTime.remove(oldestOpenTime)
        }
    }
}
