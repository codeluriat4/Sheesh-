package org.example.test.marketdata

// Lifecycle of a socket connection. Consumers branch over this with a
// polymorphic `when` instead of boolean flags scattered across classes.
sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Connecting : ConnectionState
    data object Open : ConnectionState
    data class Closing(val code: Int, val reason: String) : ConnectionState
    data class Closed(val code: Int, val reason: String) : ConnectionState
    data class Failed(val error: Throwable) : ConnectionState
}
