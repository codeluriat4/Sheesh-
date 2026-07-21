package org.example.test.domain.heatmap

// How a raw volume maps to a normalized [0, 1] intensity for coloring a
// heatmap cell. New scales are added as new implementations, never as a
// branch inside the node that uses them.
fun interface HeatmapIntensityScale {
    fun normalize(volume: Double, maxVolume: Double): Double
}

object LinearIntensityScale : HeatmapIntensityScale {
    override fun normalize(volume: Double, maxVolume: Double): Double =
        if (maxVolume <= 0.0) 0.0 else (volume / maxVolume).coerceIn(0.0, 1.0)
}

object LogarithmicIntensityScale : HeatmapIntensityScale {
    override fun normalize(volume: Double, maxVolume: Double): Double {
        if (maxVolume <= 0.0 || volume <= 0.0) return 0.0
        val numerator = kotlin.math.ln(1.0 + volume)
        val denominator = kotlin.math.ln(1.0 + maxVolume)
        return if (denominator == 0.0) 0.0 else (numerator / denominator).coerceIn(0.0, 1.0)
    }
}
