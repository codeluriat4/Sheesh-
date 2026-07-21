package org.example.test.marketdata.repository

import kotlinx.coroutines.flow.Flow
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe

// Presentation-facing contract: a continuously updated, ascending-by-time
// candle series for one timeframe. Nothing here exposes transport, wire
// format, or exchange-specific types, so a ViewModel can depend on this
// without knowing which exchange or protocol is behind it.
interface CandlestickSeriesRepository {
    fun observeCandles(timeframe: Timeframe): Flow<List<Candlestick>>
    fun start()
    fun stop()
}
