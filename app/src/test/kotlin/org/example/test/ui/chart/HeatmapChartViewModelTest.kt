package org.example.test.ui.chart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.heatmap.HeatmapTapLocation
import org.example.test.marketdata.bitget.OrderBookLevel
import org.example.test.marketdata.orderbook.OrderBookSnapshot
import org.example.test.ui.orderbook.FakeOrderBookRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HeatmapChartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val timeframe = Timeframe.OneMinute

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun candle(openTimeMillis: Long, price: Double) = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = timeframe,
        openTimeMillis = openTimeMillis,
        open = Price(price),
        high = Price(price + 1.0),
        low = Price(price - 1.0),
        close = Price(price),
        volume = Volume(1.0),
    )

    private fun tapAt(columnIndex: Int, rowIndex: Int, rowCount: Int): HeatmapTapLocation {
        val xFraction = (columnIndex + 0.5) / VisibleCandleCount.VALUE
        val rowFromTop = rowCount - 1 - rowIndex
        val yFraction = (rowFromTop + 0.5) / rowCount
        return HeatmapTapLocation(xFraction = xFraction, yFraction = yFraction)
    }

    @Test
    fun `a tap resolves and publishes the node painted at that location`() = runTest(testDispatcher) {
        val openTimeMillis = 0L
        val candlestickRepository = FakeCandlestickSeriesRepository()
        candlestickRepository.emitSeries(timeframe, listOf(candle(openTimeMillis, 100.0)))
        val candlestickViewModel = CandlestickChartViewModel(injectedRepository = candlestickRepository)

        val orderBookRepository = FakeOrderBookRepository()
        val heatmapViewModel = HeatmapChartViewModel(
            candlestickViewModel = candlestickViewModel,
            injectedOrderBookRepository = orderBookRepository,
        )
        // uiState is shared with WhileSubscribed, so its upstream (and the
        // windowState it is built from) only runs while collected.
        val collectedStates = mutableListOf<HeatmapChartUiState>()
        val collector = launch { heatmapViewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        orderBookRepository.emitSnapshot(
            OrderBookSnapshot(
                instrumentId = "BTCUSDT",
                bids = listOf(OrderBookLevel(price = 99.0, size = 10.0)),
                asks = emptyList(),
                sequence = 1L,
                updatedAtMillis = openTimeMillis,
            ),
        )
        runCurrent()

        val window = collectedStates.last().window
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        val cell = window.columns[lastColumnIndex].cells.single()
        val rowIndex = window.grid.bucketIndexOf(cell.price)

        heatmapViewModel.onTap(tapAt(columnIndex = lastColumnIndex, rowIndex = rowIndex, rowCount = window.grid.rowCount))
        runCurrent()

        assertEquals(cell, collectedStates.last().selectedNode)
        collector.cancel()
    }

    @Test
    fun `a tap on an empty cell clears any previous selection`() = runTest(testDispatcher) {
        val openTimeMillis = 0L
        val candlestickRepository = FakeCandlestickSeriesRepository()
        candlestickRepository.emitSeries(timeframe, listOf(candle(openTimeMillis, 100.0)))
        val candlestickViewModel = CandlestickChartViewModel(injectedRepository = candlestickRepository)

        val orderBookRepository = FakeOrderBookRepository()
        val heatmapViewModel = HeatmapChartViewModel(
            candlestickViewModel = candlestickViewModel,
            injectedOrderBookRepository = orderBookRepository,
        )
        val collectedStates = mutableListOf<HeatmapChartUiState>()
        val collector = launch { heatmapViewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        orderBookRepository.emitSnapshot(
            OrderBookSnapshot(
                instrumentId = "BTCUSDT",
                bids = listOf(OrderBookLevel(price = 99.0, size = 10.0)),
                asks = emptyList(),
                sequence = 1L,
                updatedAtMillis = openTimeMillis,
            ),
        )
        runCurrent()

        val window = collectedStates.last().window
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        val cell = window.columns[lastColumnIndex].cells.single()
        val rowIndex = window.grid.bucketIndexOf(cell.price)
        heatmapViewModel.onTap(tapAt(columnIndex = lastColumnIndex, rowIndex = rowIndex, rowCount = window.grid.rowCount))
        runCurrent()
        assertEquals(cell, collectedStates.last().selectedNode)

        heatmapViewModel.onTap(tapAt(columnIndex = 0, rowIndex = 0, rowCount = window.grid.rowCount))
        runCurrent()

        assertNull(collectedStates.last().selectedNode)
        collector.cancel()
    }

    @Test
    fun `clearSelection dismisses the current selection without resolving a tap`() = runTest(testDispatcher) {
        val openTimeMillis = 0L
        val candlestickRepository = FakeCandlestickSeriesRepository()
        candlestickRepository.emitSeries(timeframe, listOf(candle(openTimeMillis, 100.0)))
        val candlestickViewModel = CandlestickChartViewModel(injectedRepository = candlestickRepository)

        val orderBookRepository = FakeOrderBookRepository()
        val heatmapViewModel = HeatmapChartViewModel(
            candlestickViewModel = candlestickViewModel,
            injectedOrderBookRepository = orderBookRepository,
        )
        val collectedStates = mutableListOf<HeatmapChartUiState>()
        val collector = launch { heatmapViewModel.uiState.collect(collectedStates::add) }
        runCurrent()

        orderBookRepository.emitSnapshot(
            OrderBookSnapshot(
                instrumentId = "BTCUSDT",
                bids = listOf(OrderBookLevel(price = 99.0, size = 10.0)),
                asks = emptyList(),
                sequence = 1L,
                updatedAtMillis = openTimeMillis,
            ),
        )
        runCurrent()

        val window = collectedStates.last().window
        val lastColumnIndex = VisibleCandleCount.VALUE - 1
        val cell = window.columns[lastColumnIndex].cells.single()
        val rowIndex = window.grid.bucketIndexOf(cell.price)
        heatmapViewModel.onTap(tapAt(columnIndex = lastColumnIndex, rowIndex = rowIndex, rowCount = window.grid.rowCount))
        runCurrent()
        assertEquals(cell, collectedStates.last().selectedNode)

        heatmapViewModel.clearSelection()
        runCurrent()

        assertNull(collectedStates.last().selectedNode)
        collector.cancel()
    }
}
