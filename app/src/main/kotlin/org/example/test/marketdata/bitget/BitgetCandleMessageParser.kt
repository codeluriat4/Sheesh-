package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Candlestick
import org.example.test.domain.chart.Timeframe
import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.json.JSONArray
import org.json.JSONObject

// Owns exactly one responsibility: turning one raw text frame from the
// Bitget public WebSocket into a typed candle event, or null when the
// frame carries no candle data (e.g. a "pong" keepalive reply, or a push
// for a channel this parser doesn't recognize as a candle granularity).
interface BitgetCandleMessageParser {
    fun parse(rawText: String, instId: String): BitgetCandleEvent?
}

class JsonBitgetCandleMessageParser : BitgetCandleMessageParser {
    override fun parse(rawText: String, instId: String): BitgetCandleEvent? =
        runCatching { parseOrThrow(rawText, instId) }.getOrNull()

    private fun parseOrThrow(rawText: String, instId: String): BitgetCandleEvent? {
        if (rawText == "pong") return null
        val json = JSONObject(rawText)

        if (json.optString("event") == "subscribe") {
            val arg = json.optJSONObject("arg") ?: return null
            return BitgetCandleEvent.SubscriptionConfirmed(
                BitgetChannel(
                    instType = BitgetInstrumentType.fromWireValue(arg.optString("instType")),
                    channel = arg.optString("channel"),
                    instId = arg.optString("instId"),
                ),
            )
        }

        if (json.optString("event") == "error") {
            return BitgetCandleEvent.ServerError(
                code = json.optInt("code", -1),
                message = json.optString("msg", "unknown_error"),
            )
        }

        val arg = json.optJSONObject("arg") ?: return null
        val timeframe = BitgetCandleGranularity.timeframeOf(arg.optString("channel")) ?: return null
        val rows = json.optJSONArray("data") ?: return null
        val candles = (0 until rows.length()).map { index -> rows.getJSONArray(index).toCandlestick(instId, timeframe) }
        if (candles.isEmpty()) return null

        return BitgetCandleEvent.CandleUpdate(timeframe = timeframe, candles = candles)
    }
}

// Bitget candle rows are wire arrays: [openTimeMs, open, high, low, close,
// baseVolume, ...]. High/low are re-derived from open/high/low/close
// rather than trusted as-is, since Candlestick's own invariant requires
// high/low to bound open and close exactly, and floating point noise at
// the wire boundary must never fail that check.
private fun JSONArray.toCandlestick(instId: String, timeframe: Timeframe): Candlestick {
    val open = getString(1).toDouble()
    val wireHigh = getString(2).toDouble()
    val wireLow = getString(3).toDouble()
    val close = getString(4).toDouble()

    return Candlestick(
        instrumentId = instId,
        timeframe = timeframe,
        openTimeMillis = getString(0).toLong(),
        open = Price(open),
        high = Price(maxOf(wireHigh, open, close)),
        low = Price(minOf(wireLow, open, close).coerceAtLeast(MIN_PRICE)),
        close = Price(close),
        volume = Volume(getString(5).toDouble()),
    )
}

private const val MIN_PRICE = 0.01
