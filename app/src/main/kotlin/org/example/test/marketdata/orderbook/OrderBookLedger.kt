package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookLevel

// One side (bids or asks) of a maintained order book. Owns the price->size
// table and every mutation that can happen to it, so the merge rules for a
// side and the data they act on never drift apart into separate classes.
internal class OrderBookLedger(private val side: OrderBookSide) {
    private val sizeByPrice = LinkedHashMap<Double, Double>()

    fun replaceAll(levels: List<OrderBookLevel>) {
        sizeByPrice.clear()
        levels.forEach(::upsert)
    }

    fun mergeDelta(levels: List<OrderBookLevel>) {
        levels.forEach(::upsert)
    }

    fun snapshot(): List<OrderBookLevel> =
        side.orderedByPriority(sizeByPrice.map { (price, size) -> OrderBookLevel(price, size) })

    private fun upsert(level: OrderBookLevel) {
        if (level.size == 0.0) {
            sizeByPrice.remove(level.price)
        } else {
            sizeByPrice[level.price] = level.size
        }
    }
}
