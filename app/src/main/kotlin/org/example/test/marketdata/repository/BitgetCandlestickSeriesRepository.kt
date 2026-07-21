package org.example.test.marketdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.marketdata.bitget.BitgetCandlestickStreamService

// Composition point between the Bitget candle transport/parsing stack and
// the presentation layer: owns stream lifecycle and republishes each
// timeframe's reconstructed series as its own shared, replayed flow, so
// any number of collectors (chart, widget, preview) see the same live
// series without opening duplicate subscriptions or re-running
// reconstruction from scratch. A timeframe is only ever subscribed on the
// wire once something actually collects it, and stays subscribed for as
// long as any collector remains — driven entirely by collection, never by
// a poll loop.
class BitgetCandlestickSeriesRepository(
    private val scope: CoroutineScope,
    private val streamService: BitgetCandlestickStreamService = BitgetCandlestickStreamService(scope = scope),
    private val replaySubscriptionTimeoutMillis: Long = 5_000L,
) : CandlestickSeriesRepository {

    private val sharedSeriesByTimeframe = mutableMapOf<Timeframe, Flow<List<Candlestick>>>()

    override fun observeCandles(timeframe: Timeframe): Flow<List<Candlestick>> =
        sharedSeriesByTimeframe.getOrPut(timeframe) { sharedSeriesFor(timeframe) }

    override fun start() = streamService.start()

    override fun stop() = streamService.stop()

    private fun sharedSeriesFor(timeframe: Timeframe): Flow<List<Candlestick>> =
        CandlestickSeriesReducer(timeframe)
            .reduce(streamService.events)
            .onStart { streamService.subscribe(timeframe) }
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = replaySubscriptionTimeoutMillis),
                replay = 1,
            )
}
