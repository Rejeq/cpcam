package com.rejeq.cpcam.feature.settings

import android.graphics.ImageFormat
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.camera.repository.CameraDataRepository
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.repository.CameraRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SettingsComponent @AssistedInject constructor(
    private val appearanceRepo: AppearanceRepository,
    private val cameraRepo: CameraRepository,
    private val cameraDataRepo: CameraDataRepository,
    @ApplicationScope private val externalScope: CoroutineScope,

    @Assisted componentContext: ComponentContext,
    @Assisted val versionName: String,
    @Assisted("onFinished") val onFinished: () -> Unit,
    @Assisted("onLibraryLicensesClick") val onLibraryLicensesClick: () -> Unit,
    @Assisted("onEndpointClick") val onEndpointClick: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    val themeConfig = appearanceRepo.themeConfig
    val useDynamicColor = appearanceRepo.useDynamicColor

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedResolution = cameraDataRepo.cameraId
        .filterNotNull()
        .flatMapLatest {
            cameraRepo.getResolution(it)
        }

    val availableResolution = cameraDataRepo.getRecordResolutions(
        // TODO: Do not hardcode
        ImageFormat.YUV_420_888,
    )

    fun setThemeConfig(themeConfig: ThemeConfig) = externalScope.launch {
        appearanceRepo.setThemeConfig(themeConfig)
    }

    fun setUseDynamicColor(needUse: Boolean) = externalScope.launch {
        appearanceRepo.setUseDynamicColor(needUse)
    }

    fun setCameraResolution(resolution: Resolution?) = externalScope.launch {
        val camId = cameraDataRepo.currentCameraId
        if (camId == null) {
            Log.e(TAG, "Unable to set camera resolution: Camera id is null")
            return@launch
        }

        cameraRepo.setResolution(camId, resolution)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            versionName: String,
            @Assisted("onFinished") onFinished: () -> Unit,
            @Assisted("onLibraryLicensesClick") onLibraryLicensesClick:
            () -> Unit,
            @Assisted("onEndpointClick") onEndpointClick: () -> Unit,
        ): SettingsComponent
    }
}

private const val TAG = "SettingsComponent"
