package org.example.test.ui.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.test.ui.theme.DadaThemeTokens

// Each mode owns both the breakpoint policy that selects it and the
// arrangement it produces. Call sites never branch on width themselves;
// they ask a mode to arrange the slots, so a new breakpoint or arrangement
// is a new implementation rather than an added conditional branch.
sealed interface ChartLayoutMode {

    @Composable
    fun Arrange(slots: ChartScreenSlots)

    // Narrow screens: chart-related content stacks vertically so nothing
    // is clipped or squeezed below a usable width.
    data object Compact : ChartLayoutMode {
        @Composable
        override fun Arrange(slots: ChartScreenSlots) {
            val spacing = DadaThemeTokens.spacing
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                slots.topBar(Modifier.fillMaxWidth())
                slots.chart(Modifier.fillMaxWidth().weight(2f))
                slots.sidePanel(Modifier.fillMaxWidth().weight(1f))
                slots.bottomBar(Modifier.fillMaxWidth())
            }
        }
    }

    // Wide screens: the chart and its side panel share the available
    // width so both remain visible at once.
    data object Expanded : ChartLayoutMode {
        @Composable
        override fun Arrange(slots: ChartScreenSlots) {
            val spacing = DadaThemeTokens.spacing
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                slots.topBar(Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    slots.chart(Modifier.fillMaxHeight().weight(2f))
                    slots.sidePanel(Modifier.fillMaxHeight().weight(1f))
                }
                slots.bottomBar(Modifier.fillMaxWidth())
            }
        }
    }

    companion object {
        private val compactMaxWidth: Dp = 600.dp

        // Single source of truth for which mode a given width maps to;
        // ChartScreen defers to this instead of holding its own policy.
        fun forWidth(width: Dp): ChartLayoutMode =
            if (width < compactMaxWidth) Compact else Expanded
    }
}
