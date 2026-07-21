package org.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.test.ui.components.NeumorphicSurface
import org.example.test.ui.components.SkeletonBlock
import org.example.test.ui.theme.DadaElevation
import org.example.test.ui.theme.DadaThemeTokens

// Placeholder geometry that mirrors HomeScreen's layout element for
// element, so swapping this screen for HomeScreen never causes a visible
// content jump. Like HomeScreen, every visual property here comes from
// DadaThemeTokens; nothing is set directly.
@Composable
fun SkeletonHomeScreen() {
    val colors = DadaThemeTokens.colors
    val spacing = DadaThemeTokens.spacing
    val shapes = DadaThemeTokens.shapes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(spacing.xl),
                shape = shapes.small,
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(spacing.lg),
                shape = shapes.small,
            )

            NeumorphicSurface(
                modifier = Modifier.fillMaxSize(),
                shape = shapes.large,
                elevation = DadaElevation.Raised,
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(spacing.lg),
                        shape = shapes.small,
                    )

                    NeumorphicSurface(
                        shape = shapes.medium,
                        elevation = DadaElevation.Pressed,
                    ) {
                        Box(modifier = Modifier.size(spacing.xxl))
                    }

                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(spacing.xl),
                        shape = shapes.small,
                    )
                }
            }
        }
    }
}
