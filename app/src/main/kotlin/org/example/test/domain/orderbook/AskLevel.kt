package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// An ask: a seller queued at this price. Lower price ranks first.
data class AskLevel(override val price: Price, override val volume: Volume) : OrderBookLevel {
    override fun isBetterThan(other: OrderBookLevel): Boolean = price < other.price
}
