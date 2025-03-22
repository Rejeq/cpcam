package com.rejeq.cpcam.core.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun Context.hasPermission(permission: String): Boolean =
    ActivityCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
