package org.example.test.marketdata.orderbook

import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.orderbook.OrderBookSample

// Retains the most recent order book sample observed within each
// timeframe bucket, bounded to the number of columns a chart ever shows,
// so a heatmap can be rebuilt from history without retaining every push
// a live socket ever delivers. Confined to a single caller, mirroring
// OrderBookAccumulator's own single-writer contract.
class OrderBookSampleHistory(private val maxBuckets: Int = VisibleCandleCount.VALUE) {
    private val samplesByBucketStart = LinkedHashMap<Long, OrderBookSample>()

    // Folds one new sample into the bucket it falls into for the given
    // timeframe, evicting the oldest bucket once capacity is exceeded, and
    // returns every retained sample ordered oldest to newest.
    fun record(sample: OrderBookSample, timeframe: Timeframe): List<OrderBookSample> {
        val bucketStart = timeframe.bucketStartMillis(sample.timestampMillis)
        samplesByBucketStart[bucketStart] = sample
        trimToCapacity()
        return samplesByBucketStart.values.toList()
    }

    private fun trimToCapacity() {
        while (samplesByBucketStart.size > maxBuckets) {
            samplesByBucketStart.remove(samplesByBucketStart.keys.first())
        }
    }
}
