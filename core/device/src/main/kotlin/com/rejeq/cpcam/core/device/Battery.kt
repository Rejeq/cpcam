package com.rejeq.cpcam.core.device

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri

/**
 * Checks if battery optimizations are enabled for this app.
 *
 * @param pm The [PowerManager] instance used to check battery optimization
 *        status.
 * @return `true` if battery optimizations are ignored.
 *         `false` otherwise.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Context.isBatteryOptimizationsEnabled(pm: PowerManager): Boolean =
    pm.isIgnoringBatteryOptimizations(packageName)

/**
 * Requests the user to disable battery optimizations for this app by launching
 * the appropriate system activity.
 *
 * @return [BatteryOptimizationError] if the request could not be made,
 *         or `null` if the intent was started successfully.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Context.disableBatteryOptimizations(): BatteryOptimizationError? {
    val intent = Intent().apply {
        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        startActivity(intent)
        null
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "Unable to disable optimizations: No activity found", e)
        BatteryOptimizationError.NoActivityFound
    } catch (e: SecurityException) {
        Log.e(TAG, "Unable to disable optimizations: Security exception", e)
        BatteryOptimizationError.Unknown
    }
}

/**
 * Attempts to disable battery optimizations for this app if not already
 * disabled.
 *
 * If optimizations are already disabled, returns
 * [BatteryOptimizationError.AlreadyDisabled].
 * Otherwise, requests the user to disable battery optimizations by launching
 * the appropriate system activity.
 *
 * @return [BatteryOptimizationError] if the request could not be made or
 *         optimizations are already disabled,
 *         or `null` if the intent was started successfully.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Context.tryDisableBatteryOptimizations(): BatteryOptimizationError? {
    val pm = this.getSystemService(POWER_SERVICE) as PowerManager
    if (isBatteryOptimizationsEnabled(pm)) {
        return BatteryOptimizationError.AlreadyDisabled
    }

    return disableBatteryOptimizations()
}

enum class BatteryOptimizationError {
    Unknown,
    NoActivityFound,
    AlreadyDisabled,
}

private const val TAG = "Battery"
