package org.example.test.domain.chart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeframeSelectionTest {

    @Test
    fun `default selects one minute from the full menu`() {
        val selection = TimeframeSelection.default()

        assertEquals(Timeframe.OneMinute, selection.selected)
        assertEquals(Timeframe.ALL, selection.available)
    }

    @Test
    fun `next wraps around to the first timeframe after the last`() {
        val selection = TimeframeSelection(selected = Timeframe.OneWeek)

        assertEquals(Timeframe.OneMinute, selection.next().selected)
    }

    @Test
    fun `previous wraps around to the last timeframe before the first`() {
        val selection = TimeframeSelection(selected = Timeframe.OneMinute)

        assertEquals(Timeframe.OneWeek, selection.previous().selected)
    }

    @Test
    fun `rejects a selected timeframe outside the available menu`() {
        assertTrue(
            runCatching {
                TimeframeSelection(selected = Timeframe.OneHour, available = listOf(Timeframe.OneMinute))
            }.isFailure,
        )
    }

    @Test
    fun `select rejects a timeframe outside the available menu`() {
        val selection = TimeframeSelection(selected = Timeframe.OneMinute, available = listOf(Timeframe.OneMinute))

        assertTrue(runCatching { selection.select(Timeframe.OneHour) }.isFailure)
    }
}
