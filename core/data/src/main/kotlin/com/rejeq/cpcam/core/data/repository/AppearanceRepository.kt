package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.mapper.toDataStore
import com.rejeq.cpcam.core.data.mapper.toDynamicColorProto
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Repository managing app appearance preferences like theme and dynamic colors.
 */
class AppearanceRepository @Inject constructor(
    private val source: DataStoreSource,
) {
    /** Flow of current theme configuration */
    val themeConfig get() = source.store.map {
        it.themeConfig.fromDataStore()
    }.distinctUntilChanged()

    /**
     * Updates the app theme configuration.
     *
     * @param theme New theme configuration to apply
     * @return Result of the preference update
     */
    suspend fun setThemeConfig(theme: ThemeConfig) = source.tryEdit {
        this.themeConfig = theme.toDataStore()
    }

    /** Flow indicating if dynamic colors are enabled */
    val useDynamicColor get() = source.store.map {
        it.useDynamicColor.fromDataStore()
    }.distinctUntilChanged()

    /**
     * Enables or disables dynamic colors.
     *
     * @param useDynamicColor Whether dynamic colors should be enabled
     * @return Result of the preference update
     */
    suspend fun setUseDynamicColor(useDynamicColor: Boolean) = source.tryEdit {
        this.useDynamicColor = useDynamicColor.toDynamicColorProto()
    }

    /**
     * Indicating that permission has been launched (shown to user)
     *
     * @param perm Permission to listen
     */
    fun permissionWasLaunched(perm: String) = source.store.map {
        it.launchedPermissionsList.contains(perm)
    }.distinctUntilChanged()

    /**
     * Indicate that this permission has been triggered
     *
     * @param perm Permission that has been launched
     */
    suspend fun launchPermission(perm: String) = source.tryEdit {
        if (!launchedPermissions.contains(perm)) {
            launchedPermissions.add(perm)
        }
    }
}
