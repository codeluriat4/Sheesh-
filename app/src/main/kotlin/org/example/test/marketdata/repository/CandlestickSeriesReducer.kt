package org.example.test.marketdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.marketdata.bitget.BitgetCandleEvent

// Owns exactly one responsibility: folding a stream of raw candle pushes
// for one timeframe into a stream of ascending, presentation-ready
// series. No other class needs to know how snapshot batches and
// still-forming-bar updates combine into state.
class CandlestickSeriesReducer(private val timeframe: Timeframe) {
    fun reduce(events: Flow<BitgetCandleEvent>): Flow<List<Candlestick>> =
        events
            .filterIsInstance<BitgetCandleEvent.CandleUpdate>()
            .filter { it.timeframe == timeframe }
            .scan(CandlestickSeriesAccumulator(maxSize = VisibleCandleCount.VALUE)) { accumulator, event ->
                accumulator.fold(event.candles)
            }
            .drop(1)
            .map { it.toSeries() }
}
