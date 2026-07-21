package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe

// Everything the BTCUSDT perpetual candle stream can hand back to a
// consumer, expressed as one closed hierarchy so callers use `when`
// instead of type checks or null checks.
sealed interface BitgetCandleEvent {
    data class CandleUpdate(val timeframe: Timeframe, val candles: List<Candlestick>) : BitgetCandleEvent
    data class SubscriptionConfirmed(val channel: BitgetChannel) : BitgetCandleEvent
    data class ServerError(val code: Int?, val message: String) : BitgetCandleEvent
}
