package org.example.test.marketdata.orderbook

import org.example.test.domain.common.Price
import org.example.test.domain.common.Volume
import org.example.test.domain.orderbook.AskLevel
import org.example.test.domain.orderbook.BidLevel
import org.example.test.domain.orderbook.OrderBookSample

// Translates a transport-shaped OrderBookSnapshot into the domain's own
// OrderBookSample. Zero-or-negative-priced levels are dropped rather than
// letting Price's own invariant crash a live market data stream.
fun OrderBookSnapshot.toDomainSample(): OrderBookSample = OrderBookSample(
    timestampMillis = updatedAtMillis,
    bids = bids
        .filter { it.price > 0.0 }
        .map { BidLevel(Price(it.price), Volume(it.size.coerceAtLeast(0.0))) },
    asks = asks
        .filter { it.price > 0.0 }
        .map { AskLevel(Price(it.price), Volume(it.size.coerceAtLeast(0.0))) },
)
