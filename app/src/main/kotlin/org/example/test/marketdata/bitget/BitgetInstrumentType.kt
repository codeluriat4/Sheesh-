package org.example.test.marketdata.bitget

enum class BitgetInstrumentType(val wireValue: String) {
    SPOT("SPOT"),
    USDT_FUTURES("USDT-FUTURES"),
    COIN_FUTURES("COIN-FUTURES"),
    USDC_FUTURES("USDC-FUTURES");

    companion object {
        fun fromWireValue(value: String): BitgetInstrumentType =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: USDT_FUTURES
    }
}
