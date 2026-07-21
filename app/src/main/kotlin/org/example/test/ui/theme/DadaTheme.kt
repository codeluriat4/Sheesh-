package org.example.test.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalDadaColors = staticCompositionLocalOf { DadaDarkColorScheme }
private val LocalDadaTypography = staticCompositionLocalOf { DadaDefaultTypography }
private val LocalDadaShapes = staticCompositionLocalOf { DadaDefaultShapes }
private val LocalDadaSpacing = staticCompositionLocalOf { DadaDefaultSpacing }

// Single entry point every screen wraps its content in. Because the app is
// dark-only there is no theme parameter to branch on: one call site, one
// set of tokens, propagated implicitly to the whole tree.
@Composable
fun DadaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDadaColors provides DadaDarkColorScheme,
        LocalDadaTypography provides DadaDefaultTypography,
        LocalDadaShapes provides DadaDefaultShapes,
        LocalDadaSpacing provides DadaDefaultSpacing,
        content = content,
    )
}

// Mirrors the MaterialTheme.colors / .typography access pattern so screens
// never need to import or construct tokens themselves.
object DadaThemeTokens {
    val colors: DadaColorScheme
        @Composable get() = LocalDadaColors.current

    val typography: DadaTypography
        @Composable get() = LocalDadaTypography.current

    val shapes: DadaShapes
        @Composable get() = LocalDadaShapes.current

    val spacing: DadaSpacing
        @Composable get() = LocalDadaSpacing.current
}
