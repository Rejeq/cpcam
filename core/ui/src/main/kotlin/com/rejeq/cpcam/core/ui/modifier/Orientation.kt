package com.rejeq.cpcam.core.ui.modifier

import android.view.OrientationEventListener
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

@Composable
fun Modifier.adaptiveRotation(): Modifier {
    val orientation = LocalDeviceOrientation.current
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(orientation) {
        val current = rotationAngle.value

        val targetAngle = orientation.toRotationAngle()
        val normalizedCurrent = current.normalizedAngle()
        val delta = shortestAngleDelta(normalizedCurrent, targetAngle)

        rotationAngle.animateTo(
            targetValue = current + delta,
            animationSpec = tween(durationMillis = 500),
        )
    }

    return this.graphicsLayer { rotationZ = rotationAngle.value }
}

@Composable
fun ProvideDeviceOrientation(
    orientation: DeviceOrientation,
    content: @Composable () -> Unit,
) {
    val appContext = LocalContext.current.applicationContext
    var orientation = remember { mutableStateOf(orientation) }

    DisposableEffect(Unit) {
        val listener = object : OrientationEventListener(appContext) {
            override fun onOrientationChanged(degrees: Int) {
                orientation.value = degrees.toDeviceOrientation()
            }
        }

        listener.enable()

        onDispose {
            listener.disable()
        }
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
