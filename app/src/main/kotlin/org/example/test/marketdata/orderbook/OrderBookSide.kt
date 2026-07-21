package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookLevel

// Which side of the book a set of levels belongs to. Each side owns its
// own price ordering, so no caller branches on side to decide how to sort.
enum class OrderBookSide {
    BID {
        override fun orderedByPriority(levels: Collection<OrderBookLevel>): List<OrderBookLevel> =
            levels.sortedByDescending { it.price }
    },
    ASK {
        override fun orderedByPriority(levels: Collection<OrderBookLevel>): List<OrderBookLevel> =
            levels.sortedBy { it.price }
    },
    ;

    abstract fun orderedByPriority(levels: Collection<OrderBookLevel>): List<OrderBookLevel>
}
