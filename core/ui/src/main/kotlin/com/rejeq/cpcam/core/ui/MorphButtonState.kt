package com.rejeq.cpcam.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import kotlin.math.sqrt

enum class MorphIconTarget {
    Stopped,
    Loading,
    Started,
}

@Stable
class MorphButtonState(animTarget: MorphIconTarget) {
    var animTarget by mutableStateOf(animTarget)
    var currAnim: MorphIconTarget? by mutableStateOf(null)
    var progress by mutableStateOf(Animatable(ANIM_END))
    var isColorsInverted: Boolean by mutableStateOf(
        progress.targetValue == ANIM_END &&
            animTarget == MorphIconTarget.Loading,
    )

    private lateinit var shapes: MorphShapes

    fun getShapes(size: Size): MorphShapes {
        // FIXME: If parent composable will dynamically resize,
        // then size will not be updated
        if (!::shapes.isInitialized) {
            val minSize = size.minDimension
            shapes = MorphShapes(minSize / 2.5f, minSize / 2.0f)
        }

        return shapes
    }

    suspend fun animateTo(target: MorphIconTarget) {
        currAnim = target

        progress.animateTo(
            if (animTarget == target) ANIM_END else ANIM_START,
        )

        if (animTarget != target) {
            currAnim = null
        }
    }

    fun isAnimEnded(): Boolean =
        !progress.isRunning && progress.targetValue == ANIM_END

    companion object {
        const val ANIM_START = 0.0f
        const val ANIM_END = 1.0f
    }
}

class MorphIconColors(
    val primary: IconButtonColors,
    val inverted: IconButtonColors,
)

class MorphShapes(smallRadius: Float, largeRadius: Float) {
    val square = makeSquare(smallRadius)
    val arrow = makeArrow(smallRadius)

    // Inscribed circle within a triangle (arrow)
    val smallCircle = makeCircle((smallRadius * sqrt(3.0f)) / 3.0f)
    val largeCircle = makeCircle(largeRadius)
}

private fun makeCircle(radius: Float) = RoundedPolygon.circle(radius = radius)

private fun makeArrow(radius: Float) = RoundedPolygon(
    numVertices = 3,
    radius = radius,
    rounding = CornerRounding(
        radius * ROUND_FACTOR,
    ),
)

private fun makeSquare(size: Float) = RoundedPolygon.rectangle(
    width = size,
    height = size,
    rounding = CornerRounding(size * ROUND_FACTOR),
)

private const val ROUND_FACTOR = 1.0f / 4.0f
