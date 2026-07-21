package org.example.test.marketdata.bitget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BitgetMessageParserTest {

    private val parser: BitgetMessageParser = JsonBitgetMessageParser()

    @Test
    fun `parses a snapshot push into a typed BookUpdate with bids asks and metadata`() {
        val event = parser.parse(SNAPSHOT_FRAME)

        val update = (event as BitgetOrderBookEvent.BookUpdate).update
        assertEquals("BTCUSDT", update.instId)
        assertEquals(OrderBookAction.SNAPSHOT, update.action)
        assertEquals(listOf(OrderBookLevel(27000.0, 2.710), OrderBookLevel(26999.5, 1.460)), update.bids)
        assertEquals(listOf(OrderBookLevel(27000.5, 8.760), OrderBookLevel(27001.0, 0.400)), update.asks)
        assertEquals(123L, update.sequence)
        assertEquals(0L, update.checksum)
        assertEquals(1695716059516L, update.timestampMillis)
        assertEquals(OrderBookLevel(27000.0, 2.710), update.bestBid)
        assertEquals(OrderBookLevel(27000.5, 8.760), update.bestAsk)
    }

    @Test
    fun `parses an incremental update push with a negative checksum`() {
        val event = parser.parse(UPDATE_FRAME)

        val update = (event as BitgetOrderBookEvent.BookUpdate).update
        assertEquals(OrderBookAction.DELTA, update.action)
        assertEquals(-1638549107L, update.checksum)
        assertEquals(1628826748009L, update.timestampMillis)
    }

    @Test
    fun `parses a subscription confirmation into a typed channel`() {
        val event = parser.parse(SUBSCRIBE_CONFIRM_FRAME)

        val confirmed = event as BitgetOrderBookEvent.SubscriptionConfirmed
        assertEquals(BitgetInstrumentType.USDT_FUTURES, confirmed.channel.instType)
        assertEquals("books5", confirmed.channel.channel)
        assertEquals("BTCUSDT", confirmed.channel.instId)
    }

    @Test
    fun `parses a server error frame`() {
        val event = parser.parse(ERROR_FRAME)

        val error = event as BitgetOrderBookEvent.ServerError
        assertEquals(30001, error.code)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun `pong keepalive yields no event`() {
        assertNull(parser.parse("pong"))
    }

    @Test
    fun `malformed json yields no event instead of throwing`() {
        assertNull(parser.parse("{not-json"))
    }

    @Test
    fun `frame missing data payload yields no event`() {
        assertNull(parser.parse("""{"arg":{"instType":"USDT-FUTURES","channel":"books5","instId":"BTCUSDT"}}"""))
    }

    private companion object {
        val SNAPSHOT_FRAME = """
            {
                "action": "snapshot",
                "arg": {"instType": "USDT-FUTURES", "channel": "books5", "instId": "BTCUSDT"},
                "data": [{
                    "asks": [["27000.5", "8.760"], ["27001.0", "0.400"]],
                    "bids": [["27000.0", "2.710"], ["26999.5", "1.460"]],
                    "checksum": 0,
                    "seq": 123,
                    "ts": "1695716059516"
                }],
                "ts": 1695716059516
            }
        """.trimIndent()

        val UPDATE_FRAME = """
            {
                "action": "update",
                "arg": {"instType": "USDT-FUTURES", "channel": "books", "instId": "BTCUSDT"},
                "data": [{
                    "asks": [["44849.3", "0.0031"]],
                    "bids": [["44845.2", "0.725"]],
                    "checksum": -1638549107,
                    "seq": 124,
                    "ts": "1628826748009"
                }]
            }
        """.trimIndent()

        val SUBSCRIBE_CONFIRM_FRAME = """
            {"event": "subscribe", "arg": {"instType": "USDT-FUTURES", "channel": "books5", "instId": "BTCUSDT"}}
        """.trimIndent()

        val ERROR_FRAME = """
            {"event": "error", "code": 30001, "msg": "invalid op"}
        """.trimIndent()
    }
}
