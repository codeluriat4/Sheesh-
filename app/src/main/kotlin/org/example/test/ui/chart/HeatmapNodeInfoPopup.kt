package org.example.test.ui.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.test.R
import org.example.test.domain.heatmap.HeatmapNode
import org.example.test.ui.components.NeumorphicSurface
import org.example.test.ui.theme.DadaElevation
import org.example.test.ui.theme.DadaThemeTokens

// A small, self-contained card showing the market info for whichever
// HeatmapNode is selected: its price bucket, bid and ask volume, their
// imbalance, and how long the liquidity has rested there. Reads only the
// node it is given; it never sources selection or positions itself, so
// HeatmapChartHost stays the single place that decides where on screen
// this appears.
@Composable
fun HeatmapNodeInfoPopup(
    node: HeatmapNode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography
    val spacing = DadaThemeTokens.spacing
    val shapes = DadaThemeTokens.shapes
    val aggregated = node.aggregatedVolume

    NeumorphicSurface(
        modifier = modifier.width(POPUP_WIDTH),
        shape = shapes.small,
        elevation = DadaElevation.Floating,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "${stringResource(R.string.heatmap_node_popup_price_label)} ${formatPrice(node.price.value)}",
                    style = typography.titleMedium.copy(color = colors.onSurface),
                )
                val dismissDescription = stringResource(R.string.heatmap_node_popup_dismiss_content_description)
                BasicText(
                    text = DISMISS_GLYPH,
                    style = typography.titleMedium.copy(color = colors.onSurfaceVariant),
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .semantics { contentDescription = dismissDescription },
                )
            }

            InfoRow(
                label = stringResource(R.string.heatmap_node_popup_bid_label),
                value = formatVolume(aggregated.bidVolume.value),
                valueColor = colors.bullish,
            )
            InfoRow(
                label = stringResource(R.string.heatmap_node_popup_ask_label),
                value = formatVolume(aggregated.askVolume.value),
                valueColor = colors.bearish,
            )
            InfoRow(
                label = stringResource(R.string.heatmap_node_popup_imbalance_label),
                value = formatImbalance(aggregated.imbalance),
                valueColor = colors.onSurface,
            )
            InfoRow(
                label = stringResource(R.string.heatmap_node_popup_duration_label),
                value = formatDuration(node.duration.millis),
                valueColor = colors.onSurface,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        BasicText(text = label, style = typography.bodySmall.copy(color = colors.onSurfaceVariant))
        BasicText(text = value, style = typography.bodySmall.copy(color = valueColor, textAlign = TextAlign.End))
    }
}

private val POPUP_WIDTH = 176.dp
private const val DISMISS_GLYPH = "\u2715"

private fun formatPrice(value: Double): String = "%.2f".format(value)

private fun formatVolume(value: Double): String = "%.4f".format(value)

private fun formatImbalance(value: Double): String = "%+.1f%%".format(value * 100.0)

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
