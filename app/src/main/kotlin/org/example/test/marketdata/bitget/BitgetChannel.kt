package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Timeframe
import org.json.JSONObject

// One subscription target. Serialization to Bitget's wire representation
// lives here, next to the data it acts on.
data class BitgetChannel(
    val instType: BitgetInstrumentType,
    val channel: String,
    val instId: String,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("instType", instType.wireValue)
        .put("channel", channel)
        .put("instId", instId)

    companion object {
        // BTCUSDT perpetual (USDT-margined futures) order book, full depth.
        fun btcUsdtPerpetualOrderBook(depthChannel: String = "books"): BitgetChannel =
            BitgetChannel(BitgetInstrumentType.USDT_FUTURES, depthChannel, "BTCUSDT")

        // BTCUSDT perpetual (USDT-margined futures) candles for one timeframe.
        fun btcUsdtPerpetualCandle(timeframe: Timeframe): BitgetChannel =
            BitgetChannel(BitgetInstrumentType.USDT_FUTURES, BitgetCandleGranularity.of(timeframe).wireValue, "BTCUSDT")
    }
}
