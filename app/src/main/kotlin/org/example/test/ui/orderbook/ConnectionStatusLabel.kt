package org.example.test.ui.orderbook

import org.example.test.marketdata.ConnectionState

// Single responsibility: turn a ConnectionState into the text a screen
// shows a user. Exhaustive over the sealed type so a new lifecycle state
// fails to compile here until this mapping is updated.
val ConnectionState.displayLabel: String
    get() = when (this) {
        ConnectionState.Idle -> "Idle"
        ConnectionState.Connecting -> "Connecting"
        ConnectionState.Open -> "Live"
        is ConnectionState.Closing -> "Closing"
        is ConnectionState.Closed -> "Disconnected"
        is ConnectionState.Failed -> "Connection error"
    }
