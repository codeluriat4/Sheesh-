package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Timeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BitgetCandleMessageParserTest {

    private val parser: BitgetCandleMessageParser = JsonBitgetCandleMessageParser()

    @Test
    fun `parses a candle push into a typed CandleUpdate`() {
        val event = parser.parse(CANDLE_FRAME, instId = "BTCUSDT")

        val update = event as BitgetCandleEvent.CandleUpdate
        assertEquals(Timeframe.OneMinute, update.timeframe)
        assertEquals(1, update.candles.size)

        val candle = update.candles.single()
        assertEquals("BTCUSDT", candle.instrumentId)
        assertEquals(1695716040000L, candle.openTimeMillis)
        assertEquals(27000.0, candle.open.value, 0.0001)
        assertEquals(27015.5, candle.high.value, 0.0001)
        assertEquals(26990.0, candle.low.value, 0.0001)
        assertEquals(27005.25, candle.close.value, 0.0001)
        assertEquals(12.5, candle.volume.value, 0.0001)
    }

    @Test
    fun `parses multiple candle rows in a single push`() {
        val event = parser.parse(MULTI_ROW_FRAME, instId = "BTCUSDT")

        val update = event as BitgetCandleEvent.CandleUpdate
        assertEquals(2, update.candles.size)
        assertEquals(1695716040000L, update.candles[0].openTimeMillis)
        assertEquals(1695716100000L, update.candles[1].openTimeMillis)
    }

    @Test
    fun `parses a subscription confirmation into a typed channel`() {
        val event = parser.parse(SUBSCRIBE_CONFIRM_FRAME, instId = "BTCUSDT")

        val confirmed = event as BitgetCandleEvent.SubscriptionConfirmed
        assertEquals(BitgetInstrumentType.USDT_FUTURES, confirmed.channel.instType)
        assertEquals("candle1m", confirmed.channel.channel)
        assertEquals("BTCUSDT", confirmed.channel.instId)
    }

    @Test
    fun `parses a server error frame`() {
        val event = parser.parse(ERROR_FRAME, instId = "BTCUSDT")

        val error = event as BitgetCandleEvent.ServerError
        assertEquals(30001, error.code)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun `pong keepalive yields no event`() {
        assertNull(parser.parse("pong", instId = "BTCUSDT"))
    }

    @Test
    fun `malformed json yields no event instead of throwing`() {
        assertNull(parser.parse("{not-json", instId = "BTCUSDT"))
    }

    @Test
    fun `push for an unrecognized channel yields no event`() {
        assertNull(parser.parse(UNKNOWN_CHANNEL_FRAME, instId = "BTCUSDT"))
    }

    private companion object {
        val CANDLE_FRAME = """
            {
                "action": "update",
                "arg": {"instType": "USDT-FUTURES", "channel": "candle1m", "instId": "BTCUSDT"},
                "data": [
                    ["1695716040000", "27000.0", "27015.5", "26990.0", "27005.25", "12.5", "337565.125"]
                ],
                "ts": 1695716099000
            }
        """.trimIndent()

        val MULTI_ROW_FRAME = """
            {
                "action": "snapshot",
                "arg": {"instType": "USDT-FUTURES", "channel": "candle1m", "instId": "BTCUSDT"},
                "data": [
                    ["1695716040000", "27000.0", "27015.5", "26990.0", "27005.25", "12.5", "337565.125"],
                    ["1695716100000", "27005.25", "27020.0", "27000.0", "27010.0", "9.0", "243090.0"]
                ]
            }
        """.trimIndent()

        val SUBSCRIBE_CONFIRM_FRAME = """
            {"event": "subscribe", "arg": {"instType": "USDT-FUTURES", "channel": "candle1m", "instId": "BTCUSDT"}}
        """.trimIndent()

        val ERROR_FRAME = """
            {"event": "error", "code": 30001, "msg": "invalid op"}
        """.trimIndent()

        val UNKNOWN_CHANNEL_FRAME = """
            {
                "action": "update",
                "arg": {"instType": "USDT-FUTURES", "channel": "books", "instId": "BTCUSDT"},
                "data": [["1695716040000", "27000.0", "27015.5", "26990.0", "27005.25", "12.5"]]
            }
        """.trimIndent()
    }
}
