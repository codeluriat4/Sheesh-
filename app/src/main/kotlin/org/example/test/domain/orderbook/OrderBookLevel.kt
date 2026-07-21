package org.example.test.domain.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume

// A single priced quantity on one side of an order book. Side-specific
// priority ordering lives on the subtype, not behind a shared side flag.
sealed interface OrderBookLevel {
    val price: Price
    val volume: Volume

    // Notional exposure resting at this level; kept next to the fields it derives from.
    val notional: Double get() = price.value * volume.value

    // Whether this level ranks ahead of another level on the same side.
    fun isBetterThan(other: OrderBookLevel): Boolean
}
