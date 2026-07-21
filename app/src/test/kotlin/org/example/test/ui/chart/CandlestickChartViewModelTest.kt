package org.example.test.ui.chart

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.CandleSlot
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CandlestickChartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun candle(timeframe: Timeframe, index: Int) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = timeframe,
        openTimeMillis = index * timeframe.durationMillis,
        open = Price(100.0),
        high = Price(110.0),
        low = Price(90.0),
        close = Price(105.0),
        volume = Volume(1.0),
    )

    @Test
    fun `starting the stream happens once on creation`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()

        CandlestickChartViewModel(injectedRepository = repository)
        runCurrent()

        assertEquals(1, repository.startInvocationCount.get())
    }

    @Test
    fun `initial ui state is already exactly VisibleCandleCount slots`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()
        val viewModel = CandlestickChartViewModel(injectedRepository = repository)
        runCurrent()

        assertEquals(VisibleCandleCount.VALUE, viewModel.uiState.value.window.slots.size)
    }

    @Test
    fun `ui state reflects a newly pushed series without any manual refresh`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()
        val viewModel = CandlestickChartViewModel(injectedRepository = repository)
        val collectedStates = mutableListOf<CandlestickChartUiState>()
        val collector = launch { viewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        val series = (0 until 60).map { candle(Timeframe.OneMinute, it) }
        repository.emitSeries(Timeframe.OneMinute, series)
        runCurrent()

        val latestWindow = collectedStates.last().window
        assertEquals(VisibleCandleCount.VALUE, latestWindow.slots.size)
        assertEquals(60, latestWindow.slots.count { it is CandleSlot.Filled })
        collector.cancel()
    }

    @Test
    fun `selecting a new timeframe windows that timeframe's own series`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()
        val viewModel = CandlestickChartViewModel(injectedRepository = repository)
        // uiState is shared with WhileSubscribed, so its upstream only runs
        // while collected; reading .value without a collector attached
        // would observe the untouched initial state instead of the
        // selection made below.
        val collectedStates = mutableListOf<CandlestickChartUiState>()
        val collector = launch { viewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        val hourlySeries = (0 until VisibleCandleCount.VALUE).map { candle(Timeframe.OneHour, it) }
        repository.emitSeries(Timeframe.OneHour, hourlySeries)
        viewModel.select(Timeframe.OneHour)
        runCurrent()

        val state = collectedStates.last()
        assertEquals(Timeframe.OneHour, state.timeframeSelection.selected)
        assertEquals(Timeframe.OneHour, state.window.timeframe)
        assertTrue(state.window.slots.all { it is CandleSlot.Filled })
        collector.cancel()
    }

    @Test
    fun `switching across every quick-select timeframe in sequence updates the window each time, on one live instance`() =
        runTest(testDispatcher) {
            val repository = FakeCandlestickSeriesRepository()
            val viewModel = CandlestickChartViewModel(injectedRepository = repository)
            val collectedStates = mutableListOf<CandlestickChartUiState>()
            val collector = launch { viewModel.uiState.collect(collectedStates::add) }
            runCurrent()

            // Same ViewModel and repository instance for every switch below;
            // nothing here is torn down and recreated between timeframes,
            // which is what "no application restart" actually means at
            // this layer.
            Timeframe.QUICK_SELECT.forEach { timeframe ->
                val series = (0 until VisibleCandleCount.VALUE).map { candle(timeframe, it) }
                repository.emitSeries(timeframe, series)
                viewModel.select(timeframe)
                runCurrent()

                val state = collectedStates.last()
                assertEquals(timeframe, state.timeframeSelection.selected)
                assertEquals(timeframe, state.window.timeframe)
                assertTrue(state.window.slots.all { it is CandleSlot.Filled })
            }
            collector.cancel()
        }

    @Test
    fun `switching away from a timeframe stops the window from reflecting its later updates`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()
        val viewModel = CandlestickChartViewModel(injectedRepository = repository)
        val collectedStates = mutableListOf<CandlestickChartUiState>()
        val collector = launch { viewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        repository.emitSeries(Timeframe.OneMinute, (0 until 10).map { candle(Timeframe.OneMinute, it) })
        runCurrent()

        viewModel.select(Timeframe.FiveMinutes)
        repository.emitSeries(Timeframe.FiveMinutes, (0 until 20).map { candle(Timeframe.FiveMinutes, it) })
        runCurrent()

        // A late update to the timeframe the user just left should never
        // surface, because flatMapLatest cancelled that collection when
        // the selection changed.
        repository.emitSeries(Timeframe.OneMinute, (0 until 45).map { candle(Timeframe.OneMinute, it) })
        runCurrent()

        val state = collectedStates.last()
        assertEquals(Timeframe.FiveMinutes, state.window.timeframe)
        assertEquals(20, state.window.slots.count { it is CandleSlot.Filled })
        collector.cancel()
    }

    @Test
    fun `clearing the viewmodel stops the underlying stream`() = runTest(testDispatcher) {
        val repository = FakeCandlestickSeriesRepository()
        val viewModel = CandlestickChartViewModel(injectedRepository = repository)
        runCurrent()

        // ViewModel.clear() is internal to the androidx.lifecycle module, so
        // it can't be called directly from here; routing through a
        // ViewModelStore is the standard way to trigger onCleared() from
        // outside that module.
        val store = ViewModelStore()
        store.put("candlestick-chart", viewModel)
        store.clear()

        assertEquals(1, repository.stopInvocationCount.get())
    }
}
