package com.rejeq.cpcam.core.ui.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import com.rejeq.cpcam.core.ui.OrientationSideEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

fun Modifier.adaptiveRotation(): Modifier = this then AdaptiveRotationElement

private data object AdaptiveRotationElement :
    ModifierNodeElement<AdaptiveRotationNode>() {
    override fun create(): AdaptiveRotationNode = AdaptiveRotationNode()
    override fun update(node: AdaptiveRotationNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "adaptiveRotation"
    }
}

private class AdaptiveRotationNode :
    Modifier.Node(),
    LayoutModifierNode,
    CompositionLocalConsumerModifierNode {
    private val rotationAngle = Animatable(0f)
    private var lastOrientation: DeviceOrientation? = null
    private var job: Job? = null

    override fun onAttach() {
        tryUpdateOrientation()
    }

    override fun onDetach() {
        job?.cancelChildren()
    }

    override fun onReset() {
        lastOrientation = null
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                tryUpdateOrientation()

                rotationZ = rotationAngle.value
            }
        }
    }

    private fun tryUpdateOrientation() {
        val orientation = currentValueOf(LocalDeviceOrientation)
        if (orientation != lastOrientation) {
            launchAnimationTo(orientation)
            lastOrientation = orientation
        }
    }

    private fun launchAnimationTo(orientation: DeviceOrientation) {
        job?.cancel()
        job = coroutineScope.launch {
            val current = rotationAngle.value

            val targetAngle = orientation.toRotationAngle()
            val normalizedCurrent = current.normalizedAngle()
            val delta = shortestAngleDelta(normalizedCurrent, targetAngle)

            rotationAngle.animateTo(
                targetValue = current + delta,
                animationSpec = tween(durationMillis = 500),
            )
        }
    }
}

@Composable
fun ProvideDeviceOrientation(
    orientation: DeviceOrientation,
    content: @Composable () -> Unit,
) {
    val orientation = remember { mutableStateOf(orientation) }

    OrientationSideEffect(Unit) { degrees ->
        orientation.value = degrees.toDeviceOrientation()
    }

    CompositionLocalProvider(
        LocalDeviceOrientation provides orientation.value,
        content = content,
    )
}

@Suppress("MagicNumber")
private fun Int.toDeviceOrientation(): DeviceOrientation = when (this) {
    in 316..359, in 0..44 -> DeviceOrientation.Portrait
    in 45..135 -> DeviceOrientation.Landscape
    in 136..225 -> DeviceOrientation.ReversePortrait
    in 226..315 -> DeviceOrientation.ReverseLandscape
    else -> DeviceOrientation.Portrait
}

@Suppress("MagicNumber")
private fun DeviceOrientation.toRotationAngle(): Float = when (this) {
    DeviceOrientation.Portrait -> 0f
    DeviceOrientation.ReverseLandscape -> 90f
    DeviceOrientation.ReversePortrait -> 180f
    DeviceOrientation.Landscape -> 270f
}

/**
 * Normalizes an angle to be within the range [0, 360) degrees.
 *
 * @return The normalized angle.
 */
@Suppress("MagicNumber")
private fun Float.normalizedAngle() = (this % 360 + 360) % 360

/**
 * Computes the shortest angle delta between two angles.
 *
 * For example, moving from 0 degrees to +270 degrees could be achieved by
 * rotating +270 degrees. However, the shortest rotation is -90 degrees.
 *
 * @param from The starting angle in degrees.
 * @param to The target angle in degrees.
 * @return The shortest angle delta in degrees.
 */
@Suppress("MagicNumber")
private fun shortestAngleDelta(from: Float, to: Float): Float {
    var delta = to - from
    if (delta > 180f) {
        delta -= 360f
    } else if (delta < -180f) {
        delta += 360f
    }

    return delta
}

enum class DeviceOrientation {
    Portrait,
    ReversePortrait,
    Landscape,
    ReverseLandscape,
}

val LocalDeviceOrientation = compositionLocalOf { DeviceOrientation.Portrait }
