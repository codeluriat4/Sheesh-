package org.example.test.ui.chart

import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.heatmap.HeatmapNode
import org.example.test.domain.heatmap.HeatmapWindow

// Single presentation-ready projection of the heatmap overlay: the
// current window of bucketed liquidity cells, always built against
// whichever CandlestickWindow it is meant to sit on top of, plus
// whichever node the user last tapped, if any.
data class HeatmapChartUiState(
    val window: HeatmapWindow,
    val selectedNode: HeatmapNode? = null,
) {
    companion object {
        // Value published before any order book sample has arrived: a
        // window with no history behind it yet, aligned to whatever
        // candlestick window is currently on screen, with no selection.
        fun initial(candlestickWindow: CandlestickWindow): HeatmapChartUiState =
            HeatmapChartUiState(
                window = HeatmapWindow.fromHistory(candlestickWindow = candlestickWindow, samples = emptyList()),
            )
    }
}
