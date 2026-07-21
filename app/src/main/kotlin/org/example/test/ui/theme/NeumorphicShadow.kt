package org.example.test.ui.theme

import android.graphics.BlurMaskFilter
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Paints one blurred, offset copy of `shape` in `color`. This is the single
// primitive both the raised (outer) and pressed (inner) shadow styles below
// are built from, so neither needs its own bespoke drawing code.
private fun DrawScope.drawBlurredShape(
    shape: Shape,
    color: Color,
    blurRadius: Dp,
    offsetX: Dp,
    offsetY: Dp,
) {
    val outline = shape.createOutline(size, layoutDirection, this)
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        if (blurRadius > 0.dp) {
            maskFilter = BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
    }
    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas
        nativeCanvas.save()
        nativeCanvas.translate(offsetX.toPx(), offsetY.toPx())
        when (outline) {
            is Outline.Rounded -> {
                val rr = outline.roundRect
                nativeCanvas.drawRoundRect(
                    RectF(rr.left, rr.top, rr.right, rr.bottom),
                    rr.topLeftCornerRadius.x,
                    rr.topLeftCornerRadius.y,
                    paint,
                )
            }
            is Outline.Rectangle -> {
                val r = outline.rect
                nativeCanvas.drawRect(RectF(r.left, r.top, r.right, r.bottom), paint)
            }
            is Outline.Generic -> {
                nativeCanvas.drawPath(outline.path.asAndroidPath(), paint)
            }
        }
        nativeCanvas.restore()
    }
}

private fun Path.setFromOutline(outline: Outline): Path = apply {
    when (outline) {
        is Outline.Rounded -> addRoundRect(outline.roundRect)
        is Outline.Rectangle -> addRect(outline.rect)
        is Outline.Generic -> addPath(outline.path)
    }
}

// Outer dual shadow: a dark shadow cast away from the light source and a
// light highlight cast toward it, giving the surface a raised appearance.
private fun DrawScope.drawRaisedShadow(
    shape: Shape,
    spec: NeumorphicShadowSpec,
    colors: DadaColorScheme,
) {
    drawBlurredShape(shape, colors.shadowDark, spec.blurRadius, spec.offset, spec.offset)
    drawBlurredShape(shape, colors.shadowLight, spec.blurRadius, -spec.offset, -spec.offset)
}

// Same dual shadow, clipped to the shape's own outline so it reads as
// carved inward instead of floating above the surface.
private fun DrawScope.drawPressedShadow(
    shape: Shape,
    spec: NeumorphicShadowSpec,
    colors: DadaColorScheme,
) {
    val outline = shape.createOutline(size, layoutDirection, this)
    val clip = Path().setFromOutline(outline)
    clipPath(clip) {
        drawBlurredShape(shape, colors.shadowDark, spec.blurRadius, spec.offset, spec.offset)
        drawBlurredShape(shape, colors.shadowLight, spec.blurRadius, -spec.offset, -spec.offset)
    }
}

// The single call site that reads an elevation token: it dispatches on the
// spec's `inset` flag rather than on which DadaElevation subtype it holds,
// so new elevation levels never require another branch here.
private fun DrawScope.drawElevationShadow(
    elevation: DadaElevation,
    shape: Shape,
    colors: DadaColorScheme,
) {
    val spec = elevation.spec
    if (spec.inset) {
        drawPressedShadow(shape, spec, colors)
    } else {
        drawRaisedShadow(shape, spec, colors)
    }
}

// Public modifier: every neumorphic surface in the app renders its shadow
// through this single function, so elevation, shape and color always stay
// in sync with the active theme tokens. CompositingStrategy.Offscreen is
// required for BlurMaskFilter to render correctly on a hardware layer.
@Composable
fun Modifier.dadaElevation(elevation: DadaElevation, shape: Shape): Modifier {
    val colors = DadaThemeTokens.colors
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawBehind { drawElevationShadow(elevation, shape, colors) }
}
