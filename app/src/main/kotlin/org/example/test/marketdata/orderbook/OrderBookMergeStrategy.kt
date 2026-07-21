package org.example.test.marketdata.orderbook

import org.example.test.marketdata.bitget.OrderBookAction
import org.example.test.marketdata.bitget.OrderBookLevel

// How one incoming push should be folded into a ledger.
internal fun interface OrderBookMergeStrategy {
    fun apply(ledger: OrderBookLedger, levels: List<OrderBookLevel>)
}

// Resolves the strategy for an action by lookup rather than branching, so
// a new action type is added as one map entry with no existing dispatch
// code being touched.
internal object OrderBookMergeStrategies {
    private val strategiesByAction: Map<OrderBookAction, OrderBookMergeStrategy> = mapOf(
        OrderBookAction.SNAPSHOT to OrderBookMergeStrategy { ledger, levels -> ledger.replaceAll(levels) },
        OrderBookAction.DELTA to OrderBookMergeStrategy { ledger, levels -> ledger.mergeDelta(levels) },
    )

    private val fallback = OrderBookMergeStrategy { ledger, levels -> ledger.mergeDelta(levels) }

    fun forAction(action: OrderBookAction): OrderBookMergeStrategy =
        strategiesByAction[action] ?: fallback
}
