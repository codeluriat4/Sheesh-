package org.example.test.marketdata

// A single policy decision: how long to wait before the next reconnect
// attempt. Swapping strategies never touches socket or parsing code.
interface ReconnectStrategy {
    fun nextDelayMillis(attempt: Int): Long
    fun reset()
}
