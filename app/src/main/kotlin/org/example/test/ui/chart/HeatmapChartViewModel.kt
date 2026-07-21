package org.example.test.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.test.domain.heatmap.HeatmapNode
import org.example.test.domain.heatmap.HeatmapNodeLocator
import org.example.test.domain.heatmap.HeatmapTapLocation
import org.example.test.domain.heatmap.HeatmapWindow
import org.example.test.marketdata.orderbook.OrderBookSampleHistory
import org.example.test.marketdata.orderbook.toDomainSample
import org.example.test.marketdata.repository.BitgetOrderBookRepository
import org.example.test.marketdata.repository.OrderBookRepository

// Owns exactly one responsibility: folding live order book pushes into a
// HeatmapWindow that is always column-aligned and price-aligned to
// whatever CandlestickWindow the given CandlestickChartViewModel is
// currently publishing, so the overlay never drifts from the chart it
// sits on top of. History resets whenever the shared timeframe changes,
// since buckets from one timeframe are meaningless under another.
class HeatmapChartViewModel(
    private val candlestickViewModel: CandlestickChartViewModel,
    injectedOrderBookRepository: OrderBookRepository? = null,
) : ViewModel() {

    // Defaulting to a live repository requires viewModelScope, which is
    // only accessible once the instance exists; a constructor parameter
    // default cannot reference `this`, so the fallback is resolved here
    // in a body initializer instead.
    private val orderBookRepository: OrderBookRepository =
        injectedOrderBookRepository ?: BitgetOrderBookRepository(scope = viewModelScope)

    // The node last resolved from a tap, cleared whenever the timeframe
    // changes since a selection from one bucketing scheme is meaningless
    // under another.
    private val selectedNode = MutableStateFlow<HeatmapNode?>(null)

    private val windowState: StateFlow<HeatmapWindow> =
        candlestickViewModel.uiState
            .map { it.timeframeSelection.selected }
            .distinctUntilChanged()
            .flatMapLatest { timeframe ->
                selectedNode.value = null
                val history = OrderBookSampleHistory()
                combine(
                    candlestickViewModel.uiState,
                    orderBookRepository.observeOrderBook(),
                ) { chartState, snapshot ->
                    val samples = history.record(snapshot.toDomainSample(), timeframe)
                    HeatmapWindow.fromHistory(
                        candlestickWindow = chartState.window,
                        samples = samples,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STATE_SHARING_TIMEOUT_MILLIS),
                initialValue = HeatmapWindow.fromHistory(
                    candlestickWindow = candlestickViewModel.uiState.value.window,
                    samples = emptyList(),
                ),
            )

    val uiState: StateFlow<HeatmapChartUiState> =
        combine(windowState, selectedNode) { window, node ->
            HeatmapChartUiState(window = window, selectedNode = node)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = HeatmapChartUiState.initial(candlestickViewModel.uiState.value.window),
        )

    init {
        orderBookRepository.start()
    }

    // Resolves a tap at the given fractional canvas location against the
    // window currently on screen and publishes whatever node, if any, it
    // lands on.
    fun onTap(location: HeatmapTapLocation) {
        selectedNode.value = HeatmapNodeLocator.locate(windowState.value, location)
    }

    // Explicitly dismisses the current selection, e.g. from the info
    // popup's own dismiss affordance, without resolving any tap location.
    fun clearSelection() {
        selectedNode.value = null
    }

    override fun onCleared() {
        orderBookRepository.stop()
    }

    companion object {
        private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

        // Binds this ViewModel's construction to a specific
        // CandlestickChartViewModel instance, since the two must be kept
        // together to stay synchronized.
        fun factory(candlestickViewModel: CandlestickChartViewModel): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HeatmapChartViewModel(candlestickViewModel = candlestickViewModel) }
            }
    }
}
