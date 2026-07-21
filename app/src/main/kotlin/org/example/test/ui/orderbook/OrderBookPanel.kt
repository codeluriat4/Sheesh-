package org.example.test.ui.orderbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.example.test.R
import org.example.test.marketdata.bitget.OrderBookLevel
import org.example.test.ui.components.NeumorphicSurface
import org.example.test.ui.theme.DadaElevation
import org.example.test.ui.theme.DadaThemeTokens

// Renders an OrderBookUiState as a stacked asks/spread/bids ladder. Reads
// only the snapshot and connection state it is given; it never sources
// data itself, so a caller can preview, test, or replace the data source
// without touching this composable.
@Composable
fun OrderBookPanel(
    uiState: OrderBookUiState,
    modifier: Modifier = Modifier,
) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography
    val spacing = DadaThemeTokens.spacing
    val shapes = DadaThemeTokens.shapes

    NeumorphicSurface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.large,
        elevation = DadaElevation.Raised,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            BasicText(
                text = stringResource(R.string.order_book_title),
                style = typography.titleMedium.copy(color = colors.onSurface),
            )
            BasicText(
                text = uiState.connectionState.displayLabel,
                style = typography.labelMedium.copy(color = colors.onSurfaceVariant),
            )

            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                uiState.snapshot.asks.take(VISIBLE_LEVELS).asReversed().forEach { level ->
                    OrderBookLevelRow(level = level, color = colors.bearish)
                }
            }

            SpreadRow(spread = uiState.snapshot.spread)

            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                uiState.snapshot.bids.take(VISIBLE_LEVELS).forEach { level ->
                    OrderBookLevelRow(level = level, color = colors.bullish)
                }
            }
        }
    }
}

@Composable
private fun OrderBookLevelRow(level: OrderBookLevel, color: Color) {
    val typography = DadaThemeTokens.typography

    BasicText(
        text = "${formatPrice(level.price)}   ${formatSize(level.size)}",
        style = typography.bodySmall.copy(color = color, textAlign = TextAlign.Start),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SpreadRow(spread: Double?) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography

    BasicText(
        text = if (spread != null) {
            stringResource(R.string.order_book_spread_label, formatPrice(spread))
        } else {
            stringResource(R.string.order_book_spread_unavailable)
        },
        style = typography.labelMedium.copy(color = colors.onSurfaceVariant, textAlign = TextAlign.Center),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun formatPrice(value: Double): String = "%.2f".format(value)

private fun formatSize(value: Double): String = "%.4f".format(value)

private const val VISIBLE_LEVELS = 5
