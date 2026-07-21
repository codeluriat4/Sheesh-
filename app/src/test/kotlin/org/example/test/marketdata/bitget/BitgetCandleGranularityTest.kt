package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Timeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BitgetCandleGranularityTest {

    @Test
    fun `of returns the wire granularity for every timeframe`() {
        assertEquals("candle1m", BitgetCandleGranularity.of(Timeframe.OneMinute).wireValue)
        assertEquals("candle5m", BitgetCandleGranularity.of(Timeframe.FiveMinutes).wireValue)
        assertEquals("candle15m", BitgetCandleGranularity.of(Timeframe.FifteenMinutes).wireValue)
        assertEquals("candle30m", BitgetCandleGranularity.of(Timeframe.ThirtyMinutes).wireValue)
        assertEquals("candle1H", BitgetCandleGranularity.of(Timeframe.OneHour).wireValue)
        assertEquals("candle4H", BitgetCandleGranularity.of(Timeframe.FourHours).wireValue)
        assertEquals("candle1D", BitgetCandleGranularity.of(Timeframe.OneDay).wireValue)
        assertEquals("candle1W", BitgetCandleGranularity.of(Timeframe.OneWeek).wireValue)
    }

    @Test
    fun `timeframeOf reverses of for every known wire value`() {
        Timeframe.ALL.forEach { timeframe ->
            val wireValue = BitgetCandleGranularity.of(timeframe).wireValue
            assertEquals(timeframe, BitgetCandleGranularity.timeframeOf(wireValue))
        }
    }

    @Test
    fun `timeframeOf returns null for an unrecognized wire value`() {
        assertNull(BitgetCandleGranularity.timeframeOf("books"))
    }
}
