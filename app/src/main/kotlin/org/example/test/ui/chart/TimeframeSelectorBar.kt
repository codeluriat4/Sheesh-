package org.example.test.ui.chart

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.TimeframeSelection
import org.example.test.ui.theme.DadaThemeTokens

// Renders Timeframe.QUICK_SELECT as a row of mutually exclusive controls.
// Reads only the selection it is given and reports taps upward; it never
// sources state itself, so it can be previewed or tested without a
// ViewModel, and the one-selected-at-a-time rule lives solely in
// TimeframeSelection rather than being re-derived here.
@Composable
fun TimeframeSelectorBar(
    selection: TimeframeSelection,
    onSelect: (Timeframe) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DadaThemeTokens.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Timeframe.QUICK_SELECT.forEach { timeframe ->
            TimeframeControl(
                timeframe = timeframe,
                isSelected = timeframe == selection.selected,
                onSelect = onSelect,
            )
        }
    }
}
