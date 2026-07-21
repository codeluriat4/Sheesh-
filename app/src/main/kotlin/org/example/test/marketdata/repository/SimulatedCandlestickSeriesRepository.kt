package org.example.test.marketdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount

// Default CandlestickSeriesRepository until a real exchange feed exists.
// Owns one series per requested timeframe and republishes a freshly
// generated window on every tick, so a ViewModel sees continuously
// updated history without knowing this is simulated.
class SimulatedCandlestickSeriesRepository(
    private val scope: CoroutineScope,
    private val instrumentId: String = "BTCUSDT",
    private val generator: SimulatedCandlestickSeriesGenerator = SimulatedCandlestickSeriesGenerator(instrumentId),
    private val tickIntervalMillis: Long = 2_000L,
) : CandlestickSeriesRepository {

    private val seriesByTimeframe = mutableMapOf<Timeframe, MutableStateFlow<List<Candlestick>>>()
    private var tickerJob: Job? = null

    override fun observeCandles(timeframe: Timeframe): Flow<List<Candlestick>> = seriesFlowFor(timeframe)

    override fun start() {
        if (tickerJob != null) return
        tickerJob = scope.launch {
            while (isActive) {
                seriesByTimeframe.keys.toList().forEach(::advance)
                delay(tickIntervalMillis)
            }
        }
    }

    override fun stop() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun seriesFlowFor(timeframe: Timeframe): MutableStateFlow<List<Candlestick>> =
        seriesByTimeframe.getOrPut(timeframe) { MutableStateFlow(freshSeries(timeframe)) }

    private fun advance(timeframe: Timeframe) {
        seriesFlowFor(timeframe).value = freshSeries(timeframe)
    }

    private fun freshSeries(timeframe: Timeframe): List<Candlestick> = generator.generate(
        timeframe = timeframe,
        count = VisibleCandleCount.VALUE,
        endTimeMillis = System.currentTimeMillis(),
    )
}
