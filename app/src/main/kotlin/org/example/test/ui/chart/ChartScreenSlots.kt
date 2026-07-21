package org.example.test.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Groups the chart screen's content slots as one piece of data so layout
// modes can arrange them without knowing what each slot actually renders.
data class ChartScreenSlots(
    val topBar: @Composable (Modifier) -> Unit,
    val chart: @Composable (Modifier) -> Unit,
    val sidePanel: @Composable (Modifier) -> Unit,
    val bottomBar: @Composable (Modifier) -> Unit,
)
