package org.example.test.ui.theme

import kotlin.math.roundToInt

// Maps a normalized [0,1] position to a color. A new gradient shape is a
// new implementation, never a branch inside the code that samples one.
fun interface ColorGradient {
    fun colorAt(position: Double): RgbColor
}

// Continuous, deterministic two-stop gradient: interpolates each channel
// independently, so the same position always produces the same color.
class LinearColorGradient(
    private val start: RgbColor,
    private val end: RgbColor,
) : ColorGradient {
    override fun colorAt(position: Double): RgbColor {
        val clamped = position.coerceIn(0.0, 1.0)
        return RgbColor(
            red = interpolateChannel(start.red, end.red, clamped),
            green = interpolateChannel(start.green, end.green, clamped),
            blue = interpolateChannel(start.blue, end.blue, clamped),
        )
    }

    private fun interpolateChannel(from: Int, to: Int, position: Double): Int =
        (from + (to - from) * position).roundToInt().coerceIn(0, 255)
}

// Deep blue at minimum heat intensity, bright yellow at maximum: the
// canonical thermal gradient shared by every heat-intensity visualization
// in the app.
val ThermalColorGradient: ColorGradient = LinearColorGradient(
    start = RgbColor(red = 13, green = 27, blue = 107),
    end = RgbColor(red = 255, green = 235, blue = 59),
)
