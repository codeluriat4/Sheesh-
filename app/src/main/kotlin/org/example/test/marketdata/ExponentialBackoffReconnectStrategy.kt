package org.example.test.marketdata

import kotlin.math.min
import kotlin.math.pow

class ExponentialBackoffReconnectStrategy(
    private val baseDelayMillis: Long = 1_000L,
    private val maxDelayMillis: Long = 30_000L,
) : ReconnectStrategy {
    override fun nextDelayMillis(attempt: Int): Long {
        val exponential = baseDelayMillis * 2.0.pow(attempt.coerceAtLeast(0)).toLong()
        return min(exponential, maxDelayMillis)
    }

    override fun reset() = Unit
}
