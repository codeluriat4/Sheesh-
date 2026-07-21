package org.example.test.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorGradientTest {

    @Test
    fun `minimum position resolves to the gradient's start color`() {
        val gradient = LinearColorGradient(start = RgbColor(10, 20, 30), end = RgbColor(200, 210, 220))

        assertEquals(RgbColor(10, 20, 30), gradient.colorAt(0.0))
    }

    @Test
    fun `maximum position resolves to the gradient's end color`() {
        val gradient = LinearColorGradient(start = RgbColor(10, 20, 30), end = RgbColor(200, 210, 220))

        assertEquals(RgbColor(200, 210, 220), gradient.colorAt(1.0))
    }

    @Test
    fun `midpoint blends every channel evenly`() {
        val gradient = LinearColorGradient(start = RgbColor(0, 0, 0), end = RgbColor(200, 100, 50))

        assertEquals(RgbColor(100, 50, 25), gradient.colorAt(0.5))
    }

    @Test
    fun `positions outside zero to one are clamped`() {
        val gradient = LinearColorGradient(start = RgbColor(0, 0, 0), end = RgbColor(255, 255, 255))

        assertEquals(RgbColor(0, 0, 0), gradient.colorAt(-1.0))
        assertEquals(RgbColor(255, 255, 255), gradient.colorAt(2.0))
    }

    @Test
    fun `same position always produces the same color`() {
        assertEquals(ThermalColorGradient.colorAt(0.37), ThermalColorGradient.colorAt(0.37))
    }

    @Test
    fun `thermal gradient maps minimum heat intensity to deep blue`() {
        assertEquals(RgbColor(13, 27, 107), ThermalColorGradient.colorAt(0.0))
    }

    @Test
    fun `thermal gradient maps maximum heat intensity to bright yellow`() {
        assertEquals(RgbColor(255, 235, 59), ThermalColorGradient.colorAt(1.0))
    }
}
