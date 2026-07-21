package org.example.test.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// Composition root for the timeframe controls: resolves the same
// CandlestickChartViewModel instance HeatmapChartHost uses within
// this screen's ViewModelStoreOwner and forwards its timeframe selection
// to the pure selector bar, wiring taps back into the ViewModel. Screens
// depend on this, never on CandlestickChartViewModel or
// TimeframeSelectorBar directly, so how the selection is sourced can
// change without touching layout or rendering code.
@Composable
fun TimeframeSelectorHost(
    modifier: Modifier = Modifier,
    viewModel: CandlestickChartViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    TimeframeSelectorBar(
        selection = uiState.timeframeSelection,
        onSelect = viewModel::select,
        modifier = modifier,
    )
}
