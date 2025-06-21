package com.rejeq.cpcam.feature.scanner.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.rejeq.cpcam.core.data.model.Resolution
import kotlin.math.min

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    size: Resolution? = null,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        content()

        if (size != null) {
            val minPx = min(size.width, size.height)
            val sizeDp = with(LocalDensity.current) {
                minPx.toDp() * SQUARE_SIZE
            }

            RoundedSquare(modifier = Modifier.size(sizeDp))
        }
    }
}

@Composable
private fun RoundedSquare(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val x = 0.0f
        val y = 0.0f

        val min = min(size.width, size.height)
        val width = min
        val height = min

        val r = min * CORNER_RADIUS
        val strokeW = min * STROKE_WIDTH

        // Top‑Left
        drawRoundedCorner(
            rect = Rect(
                x,
                y,
                x + r * 2,
                y + r * 2,
            ),
            angle = 180f,
            width = strokeW,
        )

        // Top‑Right
        drawRoundedCorner(
            rect = Rect(
                x + width - 2 * r,
                y,
                x + width,
                y + 2 * r,
            ),
            angle = 270f,
            width = strokeW,
        )

        // Bottom-Right
        drawRoundedCorner(
            rect = Rect(
                x + width - 2 * r,
                y + height - 2 * r,
                x + width,
                y + height,
            ),
            angle = 0f,
            width = strokeW,
        )

        // Bottom‑Left
        drawRoundedCorner(
            rect = Rect(
                x,
                y + height - 2 * r,
                x + 2 * r,
                y + height,
            ),
            angle = 90f,
            width = strokeW,
        )
    }
}

private fun DrawScope.drawRoundedCorner(
    rect: Rect,
    angle: Float,
    width: Float,
) {
    val p = Path().apply {
        arcTo(
            rect = rect,
            startAngleDegrees = angle,
            sweepAngleDegrees = 90f,
            forceMoveTo = true,
        )
    }

    drawPath(
        p,
        Color.White,
        style = Stroke(
            width = width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

@Preview(name = "Device", device = "id:pixel_5")
@Preview(name = "50:23", device = "spec:width=1600px,height=736px")
@Preview(name = "16:9", device = "spec:width=1280px,height=720px")
@Preview(name = "11:9", device = "spec:width=352px,height=288px")
@Preview(name = "4:3", device = "spec:width=960px,height=720px")
@Preview(name = "2:1", device = "spec:width=1280px,height=720px")
@Preview(name = "1:1", device = "spec:width=720px,height=720px")
@Composable
fun ScannerOverlayPreview() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val size = Resolution(constraints.maxWidth, constraints.maxHeight)

        ScannerOverlay(size = size) {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
        }
    }
}

/**
 * Rounded-rect corner radius (in percents).
 */
private const val CORNER_RADIUS = 0.16f

/**
 * Thickness of the corner strokes (in percents).
 */
private const val STROKE_WIDTH = 0.01f

/**
 * How much space should occupy square (in percents).
 */
private const val SQUARE_SIZE = 0.8f
