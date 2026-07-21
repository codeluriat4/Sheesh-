package org.example.test.marketdata

// Owns the single responsibility of keeping a connection alive by emitting
// periodic keepalive frames. The wire format of the frame is supplied by
// the caller, so this scheduler is reusable across exchanges/protocols.
interface HeartbeatScheduler {
    fun start(sender: (String) -> Boolean)
    fun stop()
    fun onPongReceived()
}
