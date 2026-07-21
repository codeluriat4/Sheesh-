package org.example.test.ui.chart

import org.example.test.domain.chart.CandlestickWindow
import org.example.test.domain.chart.TimeframeSelection

// Single presentation-ready projection of the chart screen: the current
// fixed-size window to render, paired with the timeframe selection it was
// windowed from. Kept together so a screen never reconciles the two
// independently.
data class CandlestickChartUiState(
    val window: CandlestickWindow,
    val timeframeSelection: TimeframeSelection,
) {
    companion object {
        // Value published before the first candle series arrives; an
        // all-empty window at the default timeframe, never mutated in
        // place, only ever replaced wholesale.
        fun initial(nowMillis: Long): CandlestickChartUiState {
            val selection = TimeframeSelection.default()
            return CandlestickChartUiState(
                window = CandlestickWindow.fromSeries(
                    candles = emptyList(),
                    timeframe = selection.selected,
                    nowMillis = nowMillis,
                ),
                timeframeSelection = selection,
            )
        }
    }
}
