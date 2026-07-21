package org.example.test.domain.chart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeframeTest {

    @Test
    fun `bucket start floors a timestamp to the timeframe duration`() {
        val timestamp = 90_000L // 1.5 minutes

        assertEquals(60_000L, Timeframe.OneMinute.bucketStartMillis(timestamp))
        assertEquals(0L, Timeframe.FiveMinutes.bucketStartMillis(timestamp))
    }

    @Test
    fun `all lists every timeframe exactly once`() {
        assertEquals(Timeframe.ALL.size, Timeframe.ALL.distinct().size)
        assertEquals(8, Timeframe.ALL.size)
    }

    @Test
    fun `quick select offers exactly one minute through one hour in ascending order`() {
        assertEquals(
            listOf(
                Timeframe.OneMinute,
                Timeframe.FiveMinutes,
                Timeframe.FifteenMinutes,
                Timeframe.ThirtyMinutes,
                Timeframe.OneHour,
            ),
            Timeframe.QUICK_SELECT,
        )
    }

    @Test
    fun `quick select is a subset of all`() {
        assertTrue(Timeframe.ALL.containsAll(Timeframe.QUICK_SELECT))
    }
}
