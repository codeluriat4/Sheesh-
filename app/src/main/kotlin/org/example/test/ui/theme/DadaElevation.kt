package org.example.test.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// A single light/dark shadow pair, positive offsets pointing toward the
// bottom-right "dark" side and negative offsets toward the top-left
// "light" side. inset switches an outer (raised) shadow into an inner
// (pressed) shadow.
data class NeumorphicShadowSpec(
    val blurRadius: Dp,
    val offset: Dp,
    val inset: Boolean,
)

// Each elevation level knows how to describe its own shadow: callers never
// branch on which level they hold, they just ask for its spec.
sealed interface DadaElevation {
    val spec: NeumorphicShadowSpec

    data object Flat : DadaElevation {
        override val spec = NeumorphicShadowSpec(blurRadius = 0.dp, offset = 0.dp, inset = false)
    }

    data object Raised : DadaElevation {
        override val spec = NeumorphicShadowSpec(blurRadius = 12.dp, offset = 6.dp, inset = false)
    }

    data object Floating : DadaElevation {
        override val spec = NeumorphicShadowSpec(blurRadius = 20.dp, offset = 10.dp, inset = false)
    }

    data object Pressed : DadaElevation {
        override val spec = NeumorphicShadowSpec(blurRadius = 10.dp, offset = 5.dp, inset = true)
    }
}
