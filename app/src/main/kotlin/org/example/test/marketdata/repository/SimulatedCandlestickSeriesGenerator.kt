package org.example.test.marketdata.repository

import kotlin.random.Random
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// Produces a plausible-looking OHLCV random walk for one instrument and
// timeframe. Isolated here so the repository that owns stream lifecycle
// never also owns the arithmetic of inventing a price series.
class SimulatedCandlestickSeriesGenerator(
    private val instrumentId: String,
    private val startPrice: Double = 65_000.0,
    private val volatility: Double = 0.004,
    private val random: Random = Random.Default,
) {
    fun generate(timeframe: Timeframe, count: Int, endTimeMillis: Long): List<Candlestick> {
        val lastOpenTime = timeframe.bucketStartMillis(endTimeMillis)
        var price = startPrice
        val candles = ArrayList<Candlestick>(count)

        for (index in (count - 1) downTo 0) {
            val openTime = lastOpenTime - (index.toLong() * timeframe.durationMillis)
            val open = price
            val drift = open * volatility * ((random.nextDouble() - 0.5) * 2.0)
            val close = (open + drift).coerceAtLeast(MIN_PRICE)
            val high = maxOf(open, close) + (open * volatility * random.nextDouble())
            val low = (minOf(open, close) - (open * volatility * random.nextDouble())).coerceAtLeast(MIN_PRICE)
            val volume = MIN_VOLUME + (random.nextDouble() * VOLUME_RANGE)

            candles += Candlestick(
                instrumentId = instrumentId,
                timeframe = timeframe,
                openTimeMillis = openTime,
                open = Price(open),
                high = Price(high),
                low = Price(low),
                close = Price(close),
                volume = Volume(volume),
            )
            price = close
        }

        return candles
    }

    private companion object {
        const val MIN_PRICE = 0.01
        const val MIN_VOLUME = 1.0
        const val VOLUME_RANGE = 49.0
    }
}
