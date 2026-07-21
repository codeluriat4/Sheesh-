package org.example.test.domain.common

// A validated, immutable non-negative quantity of an asset.
@JvmInline
value class Volume(val value: Double) : Comparable<Volume> {

    init {
        require(value >= 0.0) { "Volume cannot be negative, was $value" }
    }

    val isZero: Boolean get() = value == 0.0

    operator fun plus(other: Volume): Volume = Volume(value + other.value)

    override fun compareTo(other: Volume): Int = value.compareTo(other.value)

    companion object {
        val ZERO = Volume(0.0)
        fun of(value: Double): Volume = Volume(value)
    }
}
