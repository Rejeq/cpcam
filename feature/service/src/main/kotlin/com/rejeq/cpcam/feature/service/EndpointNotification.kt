package com.rejeq.cpcam.feature.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.rejeq.cpcam.core.common.MainActivityContract
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.feature.service.EndpointService.Companion.ACTION_STOP_ENDPOINT
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Provides notification functionality for streaming service status updates.
 *
 * This file contains utilities for creating and updating notifications that
 * display the current state of the streaming service to users.
 */

fun Context.buildEndpointNotification(
    state: EndpointState,
    mainActivityContract: MainActivityContract,
): Notification {
    val closeIntent = Intent(this, EndpointService::class.java)
        .setAction(ACTION_STOP_ENDPOINT)

    val onClose = PendingIntent.getService(
        this,
        0,
        closeIntent,
        PendingIntent.FLAG_IMMUTABLE,
    )

    val openMainActivity = mainActivityContract.createIntent(this)
    val onNotificationClick = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(openMainActivity)
        val intent = getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // getPendingIntent() returns null only if
        // PendingIntent.FLAG_NO_CREATE applied
        requireNotNull(intent)
    }

    return buildInfoNotification(
        state,
        this,
        onClose,
        onNotificationClick,
    )
}

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
        is EndpointState.Failed -> res.getString(R.string.endpoint_failed)
    }

    val text = res.getString(R.string.notification_stream_state, endpointState)
    builder.setContentText(text)

    builder.setContentIntent(contentIntent)

    val closeIcon = CoreR.drawable.ic_close_24dp
    val closeText = res.getString(R.string.notification_action_close)
    builder.addAction(closeIcon, closeText, closeIntent)

    return builder.build()
}

@SuppressLint("InlinedApi")
internal fun Context.createServiceNotificationChannel() {
    val manager = NotificationManagerCompat.from(this)

    manager.createNotificationChannelsCompat(
        listOf(
            buildChannel(STREAM_SERVICE_CHANNEL, IMPORTANCE_DEFAULT) {
                setName(getString(R.string.endpoint_channel_name))
            },
        ),
    )
}

private inline fun buildChannel(
    channel: String,
    importance: Int,
    setProps: (NotificationChannelCompat.Builder.() -> Unit),
): NotificationChannelCompat {
    val builder = NotificationChannelCompat.Builder(channel, importance)
    builder.setProps()
    return builder.build()
}

private const val STREAM_SERVICE_CHANNEL = "stream_service_channel"
const val STREAM_SERVICE_ID = 1
const val NOTIFICATION_DEBOUNCE_DELAY = 1_000L
