package org.example.test.domain.chart

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.junit.Assert.assertEquals
import org.junit.Test

class CandleSlotTest {

    private val candle = Candlestick(
        instrumentId = "BTCUSDT",
        timeframe = Timeframe.OneMinute,
        openTimeMillis = 60_000L,
        open = Price(100.0),
        high = Price(110.0),
        low = Price(90.0),
        close = Price(105.0),
        volume = Volume(1.0),
    )

    @Test
    fun `Filled exposes the candle's own open time and timeframe`() {
        val slot = CandleSlot.Filled(candle)

        assertEquals(candle.openTimeMillis, slot.openTimeMillis)
        assertEquals(candle.timeframe, slot.timeframe)
    }

    @Test
    fun `fold dispatches Filled to onFilled`() {
        val slot: CandleSlot = CandleSlot.Filled(candle)

        val result = slot.fold(onFilled = { "filled" }, onEmpty = { "empty" })

        assertEquals("filled", result)
    }

    @Test
    fun `fold dispatches Empty to onEmpty`() {
        val slot: CandleSlot = CandleSlot.Empty(openTimeMillis = 0L, timeframe = Timeframe.OneMinute)

        val result = slot.fold(onFilled = { "filled" }, onEmpty = { "empty" })

        assertEquals("empty", result)
    }
}
