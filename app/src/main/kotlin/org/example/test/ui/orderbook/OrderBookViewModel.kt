package org.example.test.ui.orderbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.example.test.marketdata.repository.BitgetOrderBookRepository
import org.example.test.marketdata.repository.OrderBookRepository

// Owns exactly one responsibility: turning the repository's independent
// order-book and connection streams into a single, continuously updated
// UI state. Starts the underlying stream once on creation and stops it
// when cleared, so a composable only ever collects uiState and never
// issues a manual refresh to see new data.
class OrderBookViewModel(
    injectedRepository: OrderBookRepository? = null,
) : ViewModel() {

    // Defaulting to a live repository requires viewModelScope, which is
    // only accessible once the instance exists; a constructor parameter
    // default cannot reference `this`, so the fallback is resolved here
    // in a body initializer instead.
    private val repository: OrderBookRepository =
        injectedRepository ?: BitgetOrderBookRepository(scope = viewModelScope)

    val uiState: StateFlow<OrderBookUiState> =
        combine(
            repository.observeOrderBook(),
            repository.observeConnectionState(),
        ) { snapshot, connectionState -> OrderBookUiState(snapshot, connectionState) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STATE_SHARING_TIMEOUT_MILLIS),
                initialValue = OrderBookUiState.initial(),
            )

    init {
        repository.start()
    }

    override fun onCleared() {
        repository.stop()
    }

    companion object {
        private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

        // Compose's parameterless `viewModel()` call resolves an unspecified
        // factory via reflection, which requires a true zero-argument
        // constructor. Kotlin does not emit one for a constructor that only
        // has a defaulted parameter (the default is resolved by a synthetic
        // marker-based constructor Kotlin call sites use, not by reflection),
        // so relying on the default factory here would fail at runtime the
        // moment this ViewModel is first requested. An explicit factory that
        // calls the constructor directly sidesteps reflection entirely.
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer { OrderBookViewModel() }
        }
    }
}
