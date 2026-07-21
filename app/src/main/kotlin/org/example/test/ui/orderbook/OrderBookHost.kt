package org.example.test.ui.orderbook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// Composition root for the order book: owns the ViewModel and forwards
// its continuously updated state to the pure renderer. Screens depend on
// this, never on OrderBookViewModel or OrderBookPanel directly, so
// swapping how state is sourced never touches rendering code.
@Composable
fun OrderBookHost(
    modifier: Modifier = Modifier,
    viewModel: OrderBookViewModel = viewModel(
        factory = OrderBookViewModel.factory(),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    OrderBookPanel(uiState = uiState, modifier = modifier)
}
