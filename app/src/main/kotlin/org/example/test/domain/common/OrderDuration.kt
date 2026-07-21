package org.example.test.domain.common

// A validated, non-negative span of time, expressed in milliseconds, for
// which a price level's liquidity has rested continuously in the order
// book. Kept as its own type, next to Price and Volume, so every
// consumer that turns a duration into a proportional size shares one
// definition of that ratio instead of each re-deriving it.
@JvmInline
value class OrderDuration(val millis: Long) : Comparable<OrderDuration> {

    init {
        require(millis >= 0L) { "OrderDuration cannot be negative, was $millis" }
    }

    val isZero: Boolean get() = millis == 0L

    // This duration's share of the longest duration observed in the same
    // window, clamped to a visible minimum so a freshly-appeared node
    // never collapses to an unreadable sliver. When there is no
    // reference duration yet to compare against, a node is drawn at full
    // length rather than vanishing before any duration signal exists.
    fun fractionOf(maxDuration: OrderDuration): Float {
        if (maxDuration.millis <= 0L) return 1f
        val ratio = millis.toFloat() / maxDuration.millis.toFloat()
        return ratio.coerceIn(MIN_VISIBLE_FRACTION, 1f)
    }

    override fun compareTo(other: OrderDuration): Int = millis.compareTo(other.millis)

    companion object {
        private const val MIN_VISIBLE_FRACTION = 0.05f

        val ZERO = OrderDuration(0L)
        fun of(millis: Long): OrderDuration = OrderDuration(millis)
    }
}
