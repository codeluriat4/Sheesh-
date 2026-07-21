package org.example.test.ui.theme

// Plain RGB triple, decoupled from any UI toolkit so gradient math is
// pure data and behaves identically whether sampled from Compose, a
// unit test, or any future renderer.
data class RgbColor(val red: Int, val green: Int, val blue: Int) {
    init {
        require(red in 0..255) { "red must be within [0,255], was $red" }
        require(green in 0..255) { "green must be within [0,255], was $green" }
        require(blue in 0..255) { "blue must be within [0,255], was $blue" }
    }
}
