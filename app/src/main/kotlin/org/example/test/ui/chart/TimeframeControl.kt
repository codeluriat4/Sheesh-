package org.example.test.ui.chart

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.example.test.R
import org.example.test.domain.chart.Timeframe
import org.example.test.ui.components.NeumorphicSurface
import org.example.test.ui.theme.DadaElevation
import org.example.test.ui.theme.DadaThemeTokens

// A single timeframe's control. Its only responsibility is presenting one
// Timeframe as a selectable, accessible toggle and reporting taps; it
// never knows about the menu it belongs to or what selecting it does to
// application state.
@Composable
fun TimeframeControl(
    timeframe: Timeframe,
    isSelected: Boolean,
    onSelect: (Timeframe) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DadaThemeTokens.colors
    val typography = DadaThemeTokens.typography
    val spacing = DadaThemeTokens.spacing
    val shapes = DadaThemeTokens.shapes

    val contentDescription = stringResource(R.string.timeframe_control_content_description, timeframe.label)

    NeumorphicSurface(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = { onSelect(timeframe) },
                role = Role.RadioButton,
            )
            .semantics { this.contentDescription = contentDescription },
        shape = shapes.full,
        elevation = if (isSelected) DadaElevation.Pressed else DadaElevation.Raised,
    ) {
        BasicText(
            text = timeframe.label,
            style = typography.labelLarge.copy(
                color = if (isSelected) colors.primary else colors.onSurfaceVariant,
            ),
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        )
    }
}
