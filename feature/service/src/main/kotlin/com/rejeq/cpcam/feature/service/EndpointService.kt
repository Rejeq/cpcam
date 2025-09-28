package com.rejeq.cpcam.feature.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.github.michaelbull.result.onFailure
import com.rejeq.cpcam.core.common.MainActivityContract
import com.rejeq.cpcam.core.common.hasPermission
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Foreground service managing [EndpointHandler] lifecycle.
 *
 * The service runs in the foreground with a persistent notification
 * showing the current streaming state. It manages the endpoint connection
 * lifecycle and handles proper cleanup when stopped.
 */
@AndroidEntryPoint
class EndpointService : LifecycleService() {
    @Inject
    lateinit var endpoint: EndpointHandler

    @Inject
    lateinit var mainActivityContract: MainActivityContract

    private var prevEndpointState: EndpointState = EndpointState.Stopped
    private lateinit var endpointLauncher: EndpointLauncher

    override fun onCreate() {
        super.onCreate()

        this.createServiceNotificationChannel()
        endpointLauncher = EndpointLauncher(endpoint, this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)

        // Starting as foreground every time because this method can called
        // after stopSelf(), which removes service from foreground state
        if (runAsForeground()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent?.action
        return when (action) {
            ACTION_START_ENDPOINT -> startEndpoint()
            ACTION_STOP_ENDPOINT -> stopEndpoint()
            else -> {
                Log.e(TAG, "Unable to start: Unknown action '$action'")
                START_NOT_STICKY
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying service")
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        @Suppress("DEPRECATION")
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.locales.get(0)
        } else {
            newConfig.locale
        }

        Log.d(TAG, "Language changed: $locale")
        this.createServiceNotificationChannel()
        updateNotification(prevEndpointState)
    }

    private fun runAsForeground(): Boolean {
        // NOTE: Notification channel must already be registered at the time of
        // the service creation
        ServiceCompat.startForeground(
            this,
            STREAM_SERVICE_ID,
            buildEndpointNotification(prevEndpointState, mainActivityContract),
            getForegroundType(this),
        )

        return false
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun startEndpoint(): Int {
        endpointLauncher.launch(lifecycleScope) { result ->
            result.onFailure { err ->
                Log.e(
                    TAG,
                    "endpointLauncher failed with error, " +
                        "stopping service: $err",
                )

                stopEndpoint()
            }

            endpoint.state
                .debounce(NOTIFICATION_DEBOUNCE_DELAY)
                .collect(::updateNotification)

            assert(false) {
                "Unreachable state: " +
                    "'endpoint.state' should never stops collecting"
            }
        }

        return START_STICKY
    }

    private fun stopEndpoint(): Int {
        stopSelf()
        return START_NOT_STICKY
    }

    private fun updateNotification(state: EndpointState) {
        val manager = NotificationManagerCompat.from(this@EndpointService)
        prevEndpointState = state

        if (this.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            manager.notify(
                STREAM_SERVICE_ID,
                buildEndpointNotification(state, mainActivityContract),
            )
        }
    }

    companion object {
        const val ACTION_START_ENDPOINT = "StartEndpoint"
        const val ACTION_STOP_ENDPOINT = "StopEndpoint"

        const val REQUIRED_PERMISSION = Manifest.permission.CAMERA

        @SuppressLint("InlinedApi")
        fun getForegroundType(context: Context): Int {
            var type = 0

            if (context.hasPermission(Manifest.permission.CAMERA)) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }

            return type
        }
    }
}

private const val TAG = "EndpointService"
