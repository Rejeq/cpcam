package com.rejeq.cpcam.core.device

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.InterruptionFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class DndState {
    Enabled,
    Priority,
    Alarms,
    Total,
    Disabled,
}

class DndListener @Inject constructor(
    @ApplicationContext val context: Context,
    val notificationManager: NotificationManagerCompat,
) {
    private var receiver: BroadcastReceiver? = null

    val currentState get() =
        dndStateFrom(notificationManager.currentInterruptionFilter)

    /**
     * Checks if the receiver object has been started.
     *
     * @return `true` if the receiver is not null (started),
     *         `false` otherwise (not started).
     */
    fun isStarted(): Boolean = receiver != null

    /**
     * Starts listening for Do Not Disturb (DND) state changes.
     * When the receiver is already registered, it does nothing.
     *
     * This function registers a broadcast receiver to listen for changes in the
     * interruption filter mode (DND state) of the device.
     *
     * @param callback A lambda function that will be called whenever the DND
     *        state changes. It receives a [DndState] object as a parameter,
     *        representing the new state.
     *
     * @see DndState
     * @see NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun start(callback: (DndState) -> Unit) {
        if (receiver != null) {
            Log.w(TAG, "Unable to start: Already running")
            return
        }

        receiver = makeReceiver(callback, notificationManager)

        context.registerReceiver(
            receiver,
            IntentFilter(
                NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED,
            ),
        )

        Log.d(TAG, "Started DND listening")
    }

    /**
     * Stops listening for Do Not Disturb (DND) mode changes.
     * If no receiver is currently registered, this function does nothing.
     */
    fun stop() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
            Log.d(TAG, "Stopped DND listening")
        }
    }
}

private fun makeReceiver(
    callback: (DndState) -> Unit,
    manager: NotificationManagerCompat,
) = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !=
            NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
        ) {
            return
        }

        val state = dndStateFrom(manager.currentInterruptionFilter)
        callback(state)
    }
}

private fun dndStateFrom(@InterruptionFilter filter: Int) = when (filter) {
    NotificationManagerCompat.INTERRUPTION_FILTER_UNKNOWN -> DndState.Disabled
    NotificationManagerCompat.INTERRUPTION_FILTER_ALL -> DndState.Disabled
    NotificationManagerCompat.INTERRUPTION_FILTER_PRIORITY -> DndState.Priority
    NotificationManagerCompat.INTERRUPTION_FILTER_ALARMS -> DndState.Alarms
    NotificationManagerCompat.INTERRUPTION_FILTER_NONE -> DndState.Total
    else -> DndState.Enabled
}

private const val TAG = "DndListener"
