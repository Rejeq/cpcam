package com.rejeq.cpcam.core.ui

import android.content.Context
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
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

fun Modifier.adaptiveRotation(): Modifier = composed {
    val orientation = LocalDeviceOrientation.current

    val rotationAngle = remember { Animatable(0f) }

    // When the orientation changes, launch an animation to update the rotation.
    LaunchedEffect(orientation) {
        val targetAngle = when (orientation) {
            DeviceOrientation.Portrait -> 0f
            DeviceOrientation.ReversePortrait -> 180f
            DeviceOrientation.Landscape -> -90f
            DeviceOrientation.ReverseLandscape -> 90f
        }
        val current = rotationAngle.value
        val normalizedCurrent = (current % 360 + 360) % 360
        val normalizedTarget = (targetAngle % 360 + 360) % 360

        // Compute the delta for the shortest rotation path.
        var delta = normalizedTarget - normalizedCurrent
        if (delta > 180f) {
            delta -= 360f
        } else if (delta < -180f) {
            delta += 360f
        }

        // Animate to the new rotation value.
        rotationAngle.animateTo(
            targetValue = current + delta,
            animationSpec = tween(durationMillis = 500),
        )
    }

    this.graphicsLayer { rotationZ = rotationAngle.value }
}

@Composable
fun ProvideDeviceOrientation(
    orientation: DeviceOrientation,
    content: @Composable () -> Unit,
) {
    var orientation = remember { mutableStateOf(orientation) }

    val applicationContext = LocalContext.current.applicationContext
    DeviceOrientationListener(applicationContext) { orientation.value = it }

    CompositionLocalProvider(
        LocalDeviceOrientation provides orientation.value,
        content = content,
    )
}

@Composable
private fun DeviceOrientationListener(
    applicationContext: Context,
    onOrientationChange: (DeviceOrientation) -> Unit,
) {
    DisposableEffect(onOrientationChange) {
        val orientationEventListener = object : OrientationEventListener(
            applicationContext,
        ) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation >= 316 || orientation < 45) {
                    onOrientationChange(DeviceOrientation.Portrait)
                } else if (orientation in 136..225) {
                    onOrientationChange(DeviceOrientation.ReversePortrait)
                } else if (orientation in 45..135) {
                    onOrientationChange(DeviceOrientation.Landscape)
                } else { // if (orientation in 225..315)
                    onOrientationChange(DeviceOrientation.ReverseLandscape)
                }
            }
        }
        orientationEventListener.enable()

        onDispose {
            orientationEventListener.disable()
        }
    }
}

enum class DeviceOrientation {
    Portrait,
    ReversePortrait,
    Landscape,
    ReverseLandscape,
}

val LocalDeviceOrientation = compositionLocalOf { DeviceOrientation.Portrait }
