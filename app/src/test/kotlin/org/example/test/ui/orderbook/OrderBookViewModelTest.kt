package org.example.test.ui.orderbook

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.test.marketdata.ConnectionState
import org.example.test.marketdata.orderbook.OrderBookSnapshot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderBookViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starting the stream happens once on creation`() = runTest(testDispatcher) {
        val repository = FakeOrderBookRepository()

        OrderBookViewModel(injectedRepository = repository)
        runCurrent()

        assertEquals(1, repository.startInvocationCount.get())
    }

    @Test
    fun `ui state reflects a newly pushed snapshot without any manual refresh`() = runTest(testDispatcher) {
        val repository = FakeOrderBookRepository()
        val viewModel = OrderBookViewModel(injectedRepository = repository)
        val collectedStates = mutableListOf<OrderBookUiState>()
        val collector = launch { viewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        val pushedSnapshot = OrderBookSnapshot(
            instrumentId = "BTCUSDT",
            bids = emptyList(),
            asks = emptyList(),
            sequence = 42L,
            updatedAtMillis = 1_700_000_000_000L,
        )
        repository.emitSnapshot(pushedSnapshot)
        runCurrent()

        assertEquals(pushedSnapshot, collectedStates.last().snapshot)
        collector.cancel()
    }

    @Test
    fun `ui state reflects connection lifecycle transitions`() = runTest(testDispatcher) {
        val repository = FakeOrderBookRepository()
        val viewModel = OrderBookViewModel(injectedRepository = repository)
        val collectedStates = mutableListOf<OrderBookUiState>()
        val collector = launch { viewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        repository.emitConnectionState(ConnectionState.Open)
        runCurrent()

        assertEquals(ConnectionState.Open, collectedStates.last().connectionState)
        collector.cancel()
    }

    @Test
    fun `clearing the viewmodel stops the underlying stream`() = runTest(testDispatcher) {
        val repository = FakeOrderBookRepository()
        val viewModel = OrderBookViewModel(injectedRepository = repository)
        runCurrent()

        // ViewModel.clear() is internal to the androidx.lifecycle module, so
        // it can't be called directly from here; routing through a
        // ViewModelStore is the standard way to trigger onCleared() from
        // outside that module.
        val store = ViewModelStore()
        store.put("order-book", viewModel)
        store.clear()

        assertEquals(1, repository.stopInvocationCount.get())
    }
}
