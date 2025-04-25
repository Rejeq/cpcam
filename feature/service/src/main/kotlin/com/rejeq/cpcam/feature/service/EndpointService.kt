package com.rejeq.cpcam.feature.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.rejeq.cpcam.core.common.hasPermission
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Foreground service managing [EndpointHandler] lifecycle.
 *
 * The service runs in the foreground with a persistent notification
 * showing the current streaming state. It manages the endpoint connection
 * lifecycle and handles proper cleanup when stopped.
 */
@AndroidEntryPoint
class EndpointService : Service() {
    @Inject
    lateinit var endpoint: EndpointHandler

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    // endpoint created in onCreate(), so using lazy here
    private val endpointState by lazy {
        endpoint.infoNotificationData.onEach {
            updateNotification(it)
        }.stateIn(scope, SharingStarted.Eagerly, EndpointState.Stopped())
    }

    override fun onCreate() {
        super.onCreate()

        this.createServiceNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
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

        releaseWifiLock()
        releaseWakeLock()

        scope.launch {
            endpoint.disconnect()
        }.invokeOnCompletion {
            job.cancel()
        }

        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        @Suppress("DEPRECATION")
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.locales.get(0)
        } else {
            newConfig.locale
        }

        Log.d(TAG, "Language changed: $locale")
        updateNotification(endpointState.value)
    }

    /**
     * Starts the service in foreground mode with proper notification.
     *
     * Configures foreground service type based on permissions and
     * creates the required notification.
     *
     * @return `true` if starting in foreground failed, `false` on success
     */
    @SuppressLint("InlinedApi")
    private fun runAsForeground(): Boolean {
        var foregroundType = 0

        if (this.hasPermission(Manifest.permission.CAMERA)) {
            foregroundType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        }

        // NOTE: Notification channel must already be registered at the time of
        // the service creation
        ServiceCompat.startForeground(
            this,
            STREAM_SERVICE_ID,
            buildNotification(endpointState.value),
            foregroundType,
        )

        return false
    }

    /**
     * Initializes streaming endpoint connection.
     *
     * @return Service start mode (START_STICKY)
     */
    private fun startEndpoint(): Int {
        acquireWakeLock()
        acquireWifiLock()

        return START_STICKY
    }

    /**
     * Stops the streaming service.
     *
     * @return Service start mode (START_NOT_STICKY)
     */
    private fun stopEndpoint(): Int {
        stopSelf()
        return START_NOT_STICKY
    }

    /**
     * Updates old notification with new data
     */
    private fun updateNotification(state: EndpointState) {
        val manager = NotificationManagerCompat.from(this@EndpointService)

        if (this.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            manager.notify(
                STREAM_SERVICE_ID,
                buildNotification(state),
            )
        }
    }

    /**
     * Creates the service notification with current stream state.
     *
     * @return Configured notification for the foreground service
     */
    private fun buildNotification(state: EndpointState): Notification {
        val closeIntent = Intent(this, EndpointService::class.java)
            .setAction(ACTION_STOP_ENDPOINT)

        val onClose = PendingIntent.getService(
            this,
            0,
            closeIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        return buildInfoNotification(state, this, onClose)
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Cpcam::EndpointService",
        )

        wakeLock?.acquire()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun acquireWifiLock() {
        val wm = applicationContext.getSystemService(
            WIFI_SERVICE,
        ) as WifiManager

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiManager.WIFI_MODE_FULL_LOW_LATENCY
        } else {
            @Suppress("DEPRECATION")
            WifiManager.WIFI_MODE_FULL
        }

        wifiLock = wm.createWifiLock(mode, "Cpcam::EndpointService")
        wifiLock?.acquire()
    }

    private fun releaseWifiLock() {
        wifiLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    companion object {
        const val ACTION_START_ENDPOINT = "StartEndpoint"
        const val ACTION_STOP_ENDPOINT = "StopEndpoint"
    }
}

/**
 * Starts the [EndpointService] with streaming configuration.
 *
 * @param context Application context
 */
fun startEndpointService(context: Context) {
    val intent =
        Intent(context, EndpointService::class.java)
            .setAction(EndpointService.ACTION_START_ENDPOINT)

    ContextCompat.startForegroundService(context, intent)
}

/**
 * Stops the [EndpointService] and terminates streaming.
 *
 * @param context Application context
 */
fun stopEndpointService(context: Context) {
    val intent =
        Intent(context, EndpointService::class.java)
            .setAction(EndpointService.ACTION_STOP_ENDPOINT)

    ContextCompat.startForegroundService(context, intent)
}

@SuppressLint("InlinedApi")
private fun Context.createServiceNotificationChannel() {
    val manager = NotificationManagerCompat.from(this)

    manager.createNotificationChannelsCompat(
        listOf(
            buildChannel(STREAM_SERVICE_CHANNEL, IMPORTANCE_DEFAULT) {
                setName("Service")
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

private const val TAG = "EndpointService"
