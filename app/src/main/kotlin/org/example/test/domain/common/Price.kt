package org.example.test.domain.common

// A validated, immutable market price. Centralizes the "must be positive"
// invariant so no call site has to re-check or branch on validity.
@JvmInline
value class Price(val value: Double) : Comparable<Price> {

    init {
        require(value > 0.0) { "Price must be positive, was $value" }
    }

    operator fun plus(other: Price): Price = Price(value + other.value)
    operator fun minus(other: Price): Price = Price(value - other.value)

    override fun compareTo(other: Price): Int = value.compareTo(other.value)

    companion object {
        fun of(value: Double): Price = Price(value)
    }
}
