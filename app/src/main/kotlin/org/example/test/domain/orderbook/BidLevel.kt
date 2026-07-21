package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// A bid: a buyer queued at this price. Higher price ranks first.
data class BidLevel(override val price: Price, override val volume: Volume) : OrderBookLevel {
    override fun isBetterThan(other: OrderBookLevel): Boolean = price > other.price
}
