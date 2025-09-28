package com.rejeq.cpcam.feature.service

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import com.github.michaelbull.result.Result
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EndpointLauncher(
    private val endpoint: EndpointHandler,
    private val context: Context,
) {
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var job: Job? = null

    fun launch(
        scope: CoroutineScope,
        block: suspend (Result<Unit, EndpointErrorKind>) -> Unit,
    ) {
        job?.cancel()
        job = scope.launch {
            try {
                acquireWakeLock()
                acquireWifiLock()

                val result = endpoint.connect()
                block(result)
            } finally {
                // We should properly disconnect endpoint in case of
                // cancellation. It's acceptable because, in normal cases, it is
                // the last operation in service before destruction
                withContext(NonCancellable) {
                    endpoint.disconnect()
                }

                releaseWifiLock()
                releaseWakeLock()
            }
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

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
        val wm = context.applicationContext.getSystemService(
            Context.WIFI_SERVICE,
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
            if (it.isHeld) it.release()
        }
    }
}
