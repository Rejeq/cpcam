package com.rejeq.cpcam.ui

import android.util.Log
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.source.EditResult
import com.rejeq.cpcam.core.ui.PermissionStorage
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class RootPermissionStorage @Inject constructor(
    private val appearanceRepo: AppearanceRepository,
) : PermissionStorage {
    override suspend fun wasLaunched(permission: String): Boolean =
        appearanceRepo.permissionWasLaunched(permission).firstOrNull() ?: false

    override suspend fun launch(permission: String) {
        val res = appearanceRepo.launchPermission(permission)
        if (res !is EditResult.Success) {
            Log.w(TAG, "Unable to write launch permission: $res")
        }
    }
}

private const val TAG = "RootPermissionStorage"
