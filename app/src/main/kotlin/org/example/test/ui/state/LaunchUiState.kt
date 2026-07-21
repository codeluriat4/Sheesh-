package org.example.test.ui.state

import androidx.compose.runtime.Composable
import org.example.test.ui.chart.ChartTopBar
import org.example.test.ui.chart.HeatmapChartHost
import org.example.test.ui.chart.TimeframeSelectorHost
import org.example.test.ui.orderbook.OrderBookHost
import org.example.test.ui.screens.ChartScreen
import org.example.test.ui.screens.SkeletonHomeScreen

// Each phase of the launch flow knows how to render itself. Call sites
// invoke state.Content() polymorphically instead of branching on which
// phase is active, so adding a new phase never touches the launch screen.
sealed interface LaunchUiState {
    @Composable
    fun Content()

    data object Skeleton : LaunchUiState {
        @Composable
        override fun Content() {
            SkeletonHomeScreen()
        }
    }

    data object Ready : LaunchUiState {
        @Composable
        override fun Content() {
            ChartScreen(
                topBar = { modifier -> ChartTopBar(modifier = modifier) },
                chart = { modifier -> HeatmapChartHost(modifier = modifier) },
                sidePanel = { modifier -> OrderBookHost(modifier = modifier) },
                bottomBar = { modifier -> TimeframeSelectorHost(modifier = modifier) },
            )
        }
    }
}
