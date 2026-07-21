package org.example.test.marketdata.bitget

import org.json.JSONArray
import org.json.JSONObject

// Owns exactly one responsibility: turning one raw text frame from the
// Bitget public WebSocket into a typed event, or null when the frame
// carries no order-book data (e.g. a "pong" keepalive reply).
interface BitgetMessageParser {
    fun parse(rawText: String): BitgetOrderBookEvent?
}

class JsonBitgetMessageParser : BitgetMessageParser {
    override fun parse(rawText: String): BitgetOrderBookEvent? =
        runCatching { parseOrThrow(rawText) }.getOrNull()

    private fun parseOrThrow(rawText: String): BitgetOrderBookEvent? {
        if (rawText == "pong") return null
        val json = JSONObject(rawText)

        if (json.optString("event") == "subscribe") {
            val arg = json.optJSONObject("arg") ?: return null
            return BitgetOrderBookEvent.SubscriptionConfirmed(
                BitgetChannel(
                    instType = BitgetInstrumentType.fromWireValue(arg.optString("instType")),
                    channel = arg.optString("channel"),
                    instId = arg.optString("instId"),
                ),
            )
        }

        if (json.optString("event") == "error") {
            return BitgetOrderBookEvent.ServerError(
                code = json.optInt("code", -1),
                message = json.optString("msg", "unknown_error"),
            )
        }

        val arg = json.optJSONObject("arg") ?: return null
        val entry = json.optJSONArray("data")?.optJSONObject(0) ?: return null

        val update = OrderBookUpdate(
            instId = arg.optString("instId"),
            action = OrderBookAction.fromWireValue(json.optString("action")),
            bids = entry.optJSONArray("bids").toLevels(),
            asks = entry.optJSONArray("asks").toLevels(),
            sequence = entry.optString("seq", "0").toLongOrNull() ?: 0L,
            checksum = entry.optString("checksum", "0").toLongOrNull() ?: 0L,
            timestampMillis = entry.optString("ts", "0").toLongOrNull() ?: json.optLong("ts", 0L),
        )
        return BitgetOrderBookEvent.BookUpdate(update)
    }
}

private fun JSONArray?.toLevels(): List<OrderBookLevel> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val level = getJSONArray(index)
        OrderBookLevel(
            price = level.getString(0).toDouble(),
            size = level.getString(1).toDouble(),
        )
    }
}
