package org.example.test.ui.chart

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.marketdata.repository.CandlestickSeriesRepository

// Test double standing in for a real repository. Lets a test push a
// series for one timeframe directly and records lifecycle calls so
// assertions can check the ViewModel drives start/stop without any real
// socket or generator being involved.
class FakeCandlestickSeriesRepository : CandlestickSeriesRepository {
    private val seriesByTimeframe = mutableMapOf<Timeframe, MutableStateFlow<List<Candlestick>>>()

    val startInvocationCount = AtomicInteger(0)
    val stopInvocationCount = AtomicInteger(0)

    override fun observeCandles(timeframe: Timeframe): Flow<List<Candlestick>> =
        seriesFlowFor(timeframe).asStateFlow()

    override fun start() {
        startInvocationCount.incrementAndGet()
    }

    override fun stop() {
        stopInvocationCount.incrementAndGet()
    }

    fun emitSeries(timeframe: Timeframe, candles: List<Candlestick>) {
        seriesFlowFor(timeframe).value = candles
    }

    private fun seriesFlowFor(timeframe: Timeframe): MutableStateFlow<List<Candlestick>> =
        seriesByTimeframe.getOrPut(timeframe) { MutableStateFlow(emptyList()) }
}
