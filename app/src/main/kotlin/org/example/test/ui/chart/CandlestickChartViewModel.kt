package org.example.test.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.TimeframeSelection
import org.example.test.marketdata.repository.BitgetCandlestickSeriesRepository
import org.example.test.marketdata.repository.CandlestickSeriesRepository

// Owns exactly one responsibility: turning the repository's series for
// the currently selected timeframe into a fixed-size CandlestickWindow.
// Changing the timeframe only ever changes which series is being
// windowed; the windowing rule itself lives solely in CandlestickWindow.
class CandlestickChartViewModel(
    injectedRepository: CandlestickSeriesRepository? = null,
) : ViewModel() {

    // Defaulting to a live repository requires viewModelScope, which is
    // only accessible once the instance exists; a constructor parameter
    // default cannot reference `this`, so the fallback is resolved here
    // in a body initializer instead.
    private val repository: CandlestickSeriesRepository =
        injectedRepository ?: BitgetCandlestickSeriesRepository(scope = viewModelScope)

    private val timeframeSelection = MutableStateFlow(TimeframeSelection.default())

    val uiState: StateFlow<CandlestickChartUiState> =
        timeframeSelection
            .flatMapLatest { selection ->
                repository.observeCandles(selection.selected).map { candles ->
                    CandlestickChartUiState(
                        window = CandlestickWindow.fromSeries(
                            candles = candles,
                            timeframe = selection.selected,
                            nowMillis = System.currentTimeMillis(),
                        ),
                        timeframeSelection = selection,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STATE_SHARING_TIMEOUT_MILLIS),
                initialValue = CandlestickChartUiState.initial(nowMillis = System.currentTimeMillis()),
            )

    init {
        repository.start()
    }

    fun select(timeframe: Timeframe) {
        timeframeSelection.update { it.select(timeframe) }
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
            initializer { CandlestickChartViewModel() }
        }
    }
}
