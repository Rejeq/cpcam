package com.rejeq.cpcam.feature.settings

import android.graphics.ImageFormat
import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.camera.target.RecordCameraTarget
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsComponent @AssistedInject constructor(
    private val appearanceRepo: AppearanceRepository,
    private val cameraTarget: RecordCameraTarget,
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

    val selectedResolution = cameraTarget.resolution
    val availableResolution = cameraTarget.getSupportedResolutions(
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
        cameraTarget.setResolution(resolution)
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
