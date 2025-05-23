package com.rejeq.cpcam.feature.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.ui.R as CoreR
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Provides notification functionality for streaming service status updates.
 *
 * This file contains utilities for creating and updating notifications that
 * display the current state of the streaming service to users.
 */

/**
 * Monitor notification changes
 *
 * Sets up a flow that watches for changes in the stream state. This enables
 * real-time notification updates reflecting the current streaming status.
 */
@OptIn(FlowPreview::class)
val EndpointHandler.infoNotificationData
    get() = this.state.debounce(NOTIFICATION_DEBOUNCE_DELAY)

/**
 * Builds a notification displaying the current stream status.
 *
 * The notification is designed to be used with a foreground service,
 * providing users with visibility into the streaming state and basic
 * control.
 *
 * @param state Current endpoint state
 * @param context Application context for resource access
 * @param closeIntent PendingIntent to handle the close action
 * @return Configured [Notification] instance
 */
fun buildInfoNotification(
    state: EndpointState,
    context: Context,
    closeIntent: PendingIntent,
    contentIntent: PendingIntent,
): Notification {
    val res = context.resources

    val builder = NotificationCompat.Builder(
        context,
        STREAM_SERVICE_CHANNEL,
    )

    builder.setSmallIcon(CoreR.drawable.ic_sensors_24dp)
    builder.setContentTitle(res.getString(CoreR.string.app_name))
    builder.setOngoing(true)
    builder.setShowWhen(false)

    val endpointState = when (state) {
        is EndpointState.Started -> res.getString(R.string.endpoint_started)
        is EndpointState.Connecting ->
            res.getString(R.string.endpoint_connecting)
        is EndpointState.Stopped -> res.getString(R.string.endpoint_stopped)
    }

    val text = res.getString(R.string.notification_stream_state, endpointState)
    builder.setContentText(text)

    builder.setContentIntent(contentIntent)

    val closeIcon = CoreR.drawable.ic_close_24dp
    val closeText = res.getString(R.string.notification_action_close)
    builder.addAction(closeIcon, closeText, closeIntent)

    return builder.build()
}

const val STREAM_SERVICE_CHANNEL = "stream_service_channel"
const val STREAM_SERVICE_ID = 1
private const val NOTIFICATION_DEBOUNCE_DELAY = 1_000L
