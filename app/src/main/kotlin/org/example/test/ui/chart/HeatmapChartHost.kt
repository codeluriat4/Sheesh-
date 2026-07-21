package org.example.test.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.example.test.domain.heatmap.HeatmapNodeBoundsLocator

// Composition root for the synchronized candlestick-plus-heatmap chart:
// owns both ViewModels and stacks the two pure renderers in a single Box
// so they always share one canvas region, with the heatmap painted last
// so it sits above the candles. A PriceAxis sits to the right of that
// shared region, reading the same priceRange the candles are plotted
// against so the scale never drifts from what it labels. Both ViewModels'
// StateFlows are combined into one HeatmapChartHostUiState and collected
// as a single state, so an emission from either source triggers exactly
// one recomposition of this host rather than one per source flow. Screens
// depend on this, never on CandlestickChart, HeatmapOverlay, or PriceAxis
// directly, so how each stays in sync with the candles never leaks into
// screen composition.
@Composable
fun HeatmapChartHost(
    modifier: Modifier = Modifier,
    candlestickViewModel: CandlestickChartViewModel = viewModel(
        factory = CandlestickChartViewModel.factory(),
    ),
    heatmapViewModel: HeatmapChartViewModel = viewModel(
        factory = HeatmapChartViewModel.factory(candlestickViewModel),
    ),
) {
    // remember()'d on the two ViewModel instances rather than rebuilt every
    // recomposition, so the underlying combine() collector, and therefore
    // its upstream subscriptions to both StateFlows, stays the same one
    // across recompositions instead of restarting each time.
    val (combinedUiState, initialHostState) = remember(candlestickViewModel, heatmapViewModel) {
        val flow: Flow<HeatmapChartHostUiState> =
            combine(candlestickViewModel.uiState, heatmapViewModel.uiState, ::HeatmapChartHostUiState)
        val initial = HeatmapChartHostUiState(
            candlestick = candlestickViewModel.uiState.value,
            heatmap = heatmapViewModel.uiState.value,
        )
        flow to initial
    }
    val hostState by combinedUiState.collectAsState(initial = initialHostState)

    Row(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            var canvasSize by remember { mutableStateOf(IntSize.Zero) }
            var popupSize by remember { mutableStateOf(IntSize.Zero) }
            val selectedNode = hostState.heatmap.selectedNode

            CandlestickChart(window = hostState.candlestick.window, modifier = Modifier.matchParentSize())
            HeatmapOverlay(
                window = hostState.heatmap.window,
                selectedNode = selectedNode,
                onTap = heatmapViewModel::onTap,
                modifier = Modifier.matchParentSize().onSizeChanged { canvasSize = it },
            )

            if (selectedNode != null) {
                val bounds = remember(hostState.heatmap.window, selectedNode) {
                    HeatmapNodeBoundsLocator.locate(hostState.heatmap.window, selectedNode)
                }
                if (bounds != null) {
                    val anchorOffset = HeatmapPopupAnchor.offsetFor(bounds, canvasSize, popupSize)
                    HeatmapNodeInfoPopup(
                        node = selectedNode,
                        onDismiss = heatmapViewModel::clearSelection,
                        modifier = Modifier
                            .onSizeChanged { popupSize = it }
                            .offset { anchorOffset },
                    )
                }
            }
        }
        PriceAxis(priceRange = hostState.candlestick.window.priceRange, modifier = Modifier.fillMaxHeight())
    }
}
