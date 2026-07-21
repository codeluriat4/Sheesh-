package org.example.test.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.example.test.domain.chart.VisibleCandleCount
import org.example.test.domain.heatmap.HeatmapNode
import org.example.test.domain.heatmap.HeatmapTapLocation
import org.example.test.domain.heatmap.HeatmapWindow
import org.example.test.ui.theme.DadaThemeTokens
import org.example.test.ui.theme.ThermalColorGradient
import org.example.test.ui.theme.toComposeColor

// Paints a HeatmapWindow's cells as a grid of translucent rectangles,
// column-aligned 1:1 with CandlestickChart's own columns so the overlay
// never needs its own notion of candle spacing, and row-aligned to its
// own grid's row count. Each node's horizontal length is scaled by its
// own widthFraction, so a node's drawn width always corresponds
// directly to the resting duration of the order it represents rather
// than to a fixed column width. Draws nothing about price or time
// itself; it only turns already-bucketed cells into pixels. A tap is
// only ever translated into a fractional HeatmapTapLocation here;
// resolving that location to a node is left entirely to whoever owns
// the window's state, so this composable never re-implements hit
// resolution itself.
@Composable
fun HeatmapOverlay(
    window: HeatmapWindow,
    selectedNode: HeatmapNode? = null,
    onTap: (HeatmapTapLocation) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val rowCount = window.grid.rowCount
    val maxDuration = window.maxDuration
    val selectionColor = DadaThemeTokens.colors.primary

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onTap) {
                detectTapGestures { offset ->
                    onTap(
                        HeatmapTapLocation(
                            xFraction = (offset.x / size.width).toDouble().coerceIn(0.0, 1.0),
                            yFraction = (offset.y / size.height).toDouble().coerceIn(0.0, 1.0),
                        ),
                    )
                }
            },
    ) {
        val columnWidth = size.width / VisibleCandleCount.VALUE
        val rowHeight = size.height / rowCount

        window.columns.forEachIndexed { columnIndex, column ->
            val left = columnWidth * columnIndex
            column.cells.forEach { cell ->
                val rowIndex = window.grid.bucketIndexOf(cell.price)
                val top = size.height - ((rowIndex + 1) * rowHeight)
                val nodeWidth = columnWidth * cell.widthFraction(maxDuration)
                val topLeft = Offset(left, top)
                val cellSize = Size(nodeWidth, rowHeight)
                drawRect(
                    color = ThermalColorGradient.colorAt(cell.intensity).toComposeColor(),
                    topLeft = topLeft,
                    size = cellSize,
                )
                if (cell == selectedNode) {
                    drawRect(
                        color = selectionColor,
                        topLeft = topLeft,
                        size = cellSize,
                        style = Stroke(width = SELECTION_STROKE_WIDTH_DP.dp.toPx()),
                    )
                }
            }
        }
    }
}

private const val SELECTION_STROKE_WIDTH_DP = 2
