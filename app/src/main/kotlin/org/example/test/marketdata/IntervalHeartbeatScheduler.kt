package org.example.test.marketdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntervalHeartbeatScheduler(
    private val scope: CoroutineScope,
    private val intervalMillis: Long,
    private val pingPayload: String,
) : HeartbeatScheduler {
    private var job: Job? = null

    override fun start(sender: (String) -> Boolean) {
        stop()
        job = scope.launch {
            while (true) {
                delay(intervalMillis)
                sender(pingPayload)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    override fun onPongReceived() = Unit
}
