package com.rejeq.cpcam.feature.settings

import android.graphics.ImageFormat
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.camera.repository.CameraDataRepository
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.Framerate
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.repository.CameraRepository
import com.rejeq.cpcam.core.data.repository.ScreenRepository
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
    private val screenRepo: ScreenRepository,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFramerate = cameraDataRepo.cameraId
        .filterNotNull()
        .flatMapLatest {
            cameraRepo.getFramerate(it)
        }

    val availableFramerates = cameraDataRepo.getSupportedFramerates()

    val keepScreenAwake = screenRepo.keepScreenAwake
    val dimScreenDelay = screenRepo.dimScreenDelay

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

    fun setCameraFramerate(framerate: Framerate?) = externalScope.launch {
        val camId = cameraDataRepo.currentCameraId
        if (camId == null) {
            Log.e(TAG, "Unable to set camera resolution: Camera id is null")
            return@launch
        }

        cameraRepo.setFramerate(camId, framerate)
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        externalScope.launch {
            screenRepo.setKeepScreenAwake(enabled)
        }
    }

    fun setDimScreenDelay(timeMs: Long) {
        externalScope.launch {
            screenRepo.setDimScreenDelay(timeMs)
        }
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
