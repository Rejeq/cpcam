package com.rejeq.cpcam.core.ui

import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.platform.LocalContext

/**
 * A Composable that provides a side effect for observing device orientation
 * changes, ensuring proper resource management.
 *
 * @param key1 A key that, if changed, will cause the effect to be disposed and
 *        restarted.
 * @param effect A lambda function that will be invoked with the device
 *        orientation in degrees whenever it changes.
 *        The degrees value will be between 0 and 359
 *        or ORIENTATION_UNKNOWN if fails to determine.
 *
 * @see OrientationEventListener.ORIENTATION_UNKNOWN
 * @see OrientationEventListener.onOrientationChanged
 */
@Composable
@NonRestartableComposable
fun OrientationSideEffect(key1: Any?, effect: (degrees: Int) -> Unit) {
    val appContext = LocalContext.current.applicationContext

    DisposableEffect(key1) {
        val listener = object : OrientationEventListener(appContext) {
            override fun onOrientationChanged(degrees: Int) {
                effect(degrees)
            }
        }

        listener.enable()

        onDispose {
            listener.disable()
        }
    }
}
