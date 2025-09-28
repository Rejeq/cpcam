package com.rejeq.cpcam.core.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Convenient way to check if the application has a specific permission.
 *
 * @param permission The permission to check
 *        (e.g., `android.Manifest.permission.CAMERA`).
 * @return `true` if the permission is granted, `false` otherwise.
 *
 * @see ContextCompat.checkSelfPermission
 */
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
