package com.rejeq.cpcam.core.camera.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * A modifier that fits the content to the available width while maintaining
 * aspect ratio.
 *
 * The content will be stretched or shrunk to match the available width, and the
 * height will be adjusted proportionally. The content will be centered within
 * the available space.
 */
fun Modifier.fitToScreen(): Modifier = this then FitToScreenModifier()

private class FitToScreenModifier : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(Constraints())

        val ratio = placeable.width.toFloat() / placeable.height.toFloat()
        val contentSize = if (ratio > 1.0f) {
            IntSize(placeable.height, placeable.width)
        } else {
            IntSize(placeable.width, placeable.height)
        }

        val scale = constraints.maxWidth.toFloat() / contentSize.width

        val newWidth = constraints.maxWidth
        val newHeight = (contentSize.height * scale).roundToInt()

        val newConstraints = Constraints.fixed(newWidth, newHeight)
        val finalPlaceable = measurable.measure(newConstraints)

        return layout(newWidth, newHeight) {
            finalPlaceable.place(0, 0)
        }
    }
}
