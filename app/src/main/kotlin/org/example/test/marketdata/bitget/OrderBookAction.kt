package org.example.test.marketdata.bitget

enum class OrderBookAction(val wireValue: String) {
    SNAPSHOT("snapshot"),
    DELTA("update");

    companion object {
        fun fromWireValue(value: String): OrderBookAction =
            entries.firstOrNull { it.wireValue == value } ?: SNAPSHOT
    }
}
