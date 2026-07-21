package org.example.test.ui.chart

// The single state a HeatmapChartHost needs to render one frame: the
// synchronized candlestick and heatmap states it renders. Combining both
// ViewModels' StateFlows into one of these before collecting means the
// host recomposes exactly once per synchronized update instead of once
// per source flow.
data class HeatmapChartHostUiState(
    val candlestick: CandlestickChartUiState,
    val heatmap: HeatmapChartUiState,
)
