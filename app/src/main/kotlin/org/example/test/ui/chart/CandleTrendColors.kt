package org.example.test.ui.chart

import androidx.compose.ui.graphics.Color
import org.example.test.domain.chart.CandleTrend
import org.example.test.ui.theme.DadaColorScheme

// Resolves which palette color a trend paints with via a lookup table,
// the same pattern CandleTrend.from itself uses, rather than branching on
// the trend with if/else or when.
private val TREND_COLOR_SELECTORS: Map<CandleTrend, (DadaColorScheme) -> Color> = mapOf(
    CandleTrend.Bullish to { colors -> colors.bullish },
    CandleTrend.Bearish to { colors -> colors.bearish },
    CandleTrend.Neutral to { colors -> colors.onSurfaceVariant },
)

fun CandleTrend.colorIn(colors: DadaColorScheme): Color = TREND_COLOR_SELECTORS.getValue(this)(colors)
