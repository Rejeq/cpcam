package com.rejeq.cpcam.feature.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.rejeq.cpcam.core.common.hasPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EndpointServiceComponent @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Starts the [EndpointService] with streaming configuration.
     */
    fun start(): EndpointServiceError? {
        if (EndpointService.getForegroundType(context) == 0) {
            Log.e(TAG, "Unable to start: Doesn't have permissions")
            return EndpointServiceError.NoCameraPermission
        }

        val intent = Intent(context, EndpointService::class.java)
            .setAction(EndpointService.ACTION_START_ENDPOINT)

        ContextCompat.startForegroundService(context, intent)
        return null
    }

    /**
     * Stops the [EndpointService] and terminates streaming.
     */
    fun stop(): EndpointServiceError? {
        if (EndpointService.getForegroundType(context) == 0) {
            Log.e(TAG, "Unable to stop: Doesn't have permissions")
            return EndpointServiceError.NoCameraPermission
        }

        val intent = Intent(context, EndpointService::class.java)
            .setAction(EndpointService.ACTION_STOP_ENDPOINT)

        ContextCompat.startForegroundService(context, intent)
        return null
    }

    val requiredPermissions get() = EndpointService.REQUIRED_PERMISSION

    fun hasPermissions(): Boolean =
        context.hasPermission(EndpointService.REQUIRED_PERMISSION)

    enum class EndpointServiceError {
        NoCameraPermission,
    }
}

private const val TAG = "EndpointServiceCmp"
