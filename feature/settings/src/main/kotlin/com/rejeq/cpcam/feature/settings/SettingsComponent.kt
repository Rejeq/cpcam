package com.rejeq.cpcam.feature.settings

import android.graphics.ImageFormat
import android.os.Build
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.camera.operation.CameraOpExecutor
import com.rejeq.cpcam.core.camera.operation.GetCameraIdOp
import com.rejeq.cpcam.core.camera.operation.GetRecordResolutionsOp
import com.rejeq.cpcam.core.camera.operation.GetSupportedFrameratesOp
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.Framerate
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.repository.CameraRepository
import com.rejeq.cpcam.core.data.repository.ScreenRepository
import com.rejeq.cpcam.core.data.source.EditResult
import com.rejeq.cpcam.core.device.Locale
import com.rejeq.cpcam.core.device.getCurrentAppLocale
import com.rejeq.cpcam.core.device.setAppLocale
import com.rejeq.cpcam.core.device.tryDisableBatteryOptimizations
import com.rejeq.cpcam.core.ui.SnackbarDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface SettingsComponent : ChildComponent {
    val themeConfig: StateFlow<ThemeConfig?>
    val useDynamicColor: StateFlow<Boolean?>
    val currentLocale: StateFlow<Locale?>
    val selectedResolution: StateFlow<Resolution?>
    val availableResolution: StateFlow<List<Resolution>>
    val selectedFramerate: StateFlow<Framerate?>
    val availableFramerates: StateFlow<List<Framerate>>
    val keepScreenAwake: StateFlow<Boolean?>
    val dimScreenDelay: StateFlow<TextFieldValue>
    val versionName: String

    fun onThemeConfigChange(themeConfig: ThemeConfig)
    fun onUseDynamicColorChange(needUse: Boolean)
    fun onLocaleChange(newLocale: Locale)
    fun onCameraResolutionChange(resolution: Resolution?)
    fun onCameraFramerateChange(framerate: Framerate?)
    fun onKeepScreenAwakeChange(enabled: Boolean)
    fun onDimScreenDelayChange(time: TextFieldValue)
    fun onDisableBatteryOptimizationsClick()

    fun onFinished()
    fun onLibraryLicensesClick()
    fun onEndpointClick()
}

class DefaultSettingsComponent @AssistedInject constructor(
    private val appearanceRepo: AppearanceRepository,
    private val cameraRepo: CameraRepository,
    private val screenRepo: ScreenRepository,
    @ApplicationScope private val externalScope: CoroutineScope,
    private val snackbarDispatcher: SnackbarDispatcher,
    camOpExecutor: CameraOpExecutor,
    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onFinished") val onFinished: () -> Unit,
    @Assisted("onLibraryLicensesClick") val onLibraryLicensesClick: () -> Unit,
    @Assisted("onEndpointClick") val onEndpointClick: () -> Unit,
    @Assisted("onAppRestart") val onAppRestart: () -> Unit,
    @Assisted override val versionName: String,
) : SettingsComponent,
    ComponentContext by componentContext,
    CameraOpExecutor by camOpExecutor {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    override val themeConfig = appearanceRepo.themeConfig.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )
    override val useDynamicColor = appearanceRepo.useDynamicColor.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    // NOTE: getCurrentAppLocale should not be invoked in activity onCreate()
    private val _currentLocale = MutableStateFlow(getCurrentAppLocale())
    override val currentLocale = _currentLocale.asStateFlow()

    private val cameraId: StateFlow<String?> = GetCameraIdOp().invoke()
        .stateIn(
            scope,
            started = SharingStarted.Eagerly,
            null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedResolution = cameraId
        .filterNotNull()
        .flatMapLatest {
            cameraRepo.getResolution(it)
        }.stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )

    override val availableResolution = GetRecordResolutionsOp(
        // TODO: Do not hardcode
        ImageFormat.YUV_420_888,
    ).invoke().stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedFramerate = cameraId
        .filterNotNull()
        .flatMapLatest {
            cameraRepo.getFramerate(it)
        }.stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )

    override val availableFramerates = GetSupportedFrameratesOp().invoke()
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    override val keepScreenAwake = screenRepo.keepScreenAwake.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private var dimScreenDelayChangeJob: Job? = null
    private val _dimScreenDelay = MutableStateFlow(TextFieldValue())
    override val dimScreenDelay = _dimScreenDelay.asStateFlow()

    init {
        scope.launch {
            val delay = screenRepo.dimScreenDelay.first()
            val delaySec = delay?.inWholeSeconds

            _dimScreenDelay.value = TextFieldValue(delaySec?.toString() ?: "")
        }
    }

    override fun onThemeConfigChange(themeConfig: ThemeConfig) {
        safePrefWriteLaunch {
            appearanceRepo.setThemeConfig(themeConfig)
        }
    }

    override fun onUseDynamicColorChange(needUse: Boolean) {
        safePrefWriteLaunch {
            appearanceRepo.setUseDynamicColor(needUse)
        }
    }

    override fun onLocaleChange(newLocale: Locale) {
        scope.launch {
            setAppLocale(newLocale.tag)
            _currentLocale.value = newLocale
        }
    }

    override fun onCameraResolutionChange(resolution: Resolution?) {
        safePrefWriteLaunch {
            val camId = cameraId.value
            if (camId == null) {
                Log.e(TAG, "Unable to set camera resolution: Camera id is null")
                return@safePrefWriteLaunch EditResult.FailWrite
            }

            cameraRepo.setResolution(camId, resolution)
        }
    }

    override fun onCameraFramerateChange(framerate: Framerate?) {
        safePrefWriteLaunch {
            val camId = cameraId.value
            if (camId == null) {
                Log.e(TAG, "Unable to set camera resolution: Camera id is null")
                return@safePrefWriteLaunch EditResult.FailWrite
            }

            cameraRepo.setFramerate(camId, framerate)
        }
    }

    override fun onKeepScreenAwakeChange(enabled: Boolean) {
        safePrefWriteLaunch {
            screenRepo.setKeepScreenAwake(enabled)
        }
    }

    override fun onDimScreenDelayChange(time: TextFieldValue) {
        _dimScreenDelay.value = time

        dimScreenDelayChangeJob?.cancel()
        dimScreenDelayChangeJob = safePrefWriteLaunch {
            val delay = time.text.trim().toLongOrNull()
            screenRepo.setDimScreenDelay(
                delay?.toDuration(DurationUnit.SECONDS),
            )
        }
    }

    override fun onDisableBatteryOptimizationsClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Unable to disable battery optimizations: API < 23")
            return
        }

        scope.launch {
            val err = context.tryDisableBatteryOptimizations()

            err?.toSnackbarState()
                ?.let { state ->
                    snackbarDispatcher.show(state)
                }
        }
    }

    override fun onFinished() = onFinished.invoke()
    override fun onLibraryLicensesClick() = onLibraryLicensesClick.invoke()
    override fun onEndpointClick() = onEndpointClick.invoke()

    private fun safePrefWriteLaunch(
        retryCount: Int = 0,
        block: suspend () -> EditResult,
    ): Job = externalScope.launch {
        when (block()) {
            is EditResult.Success -> { /* Do nothing on success */ }
            is EditResult.FailWrite -> handlePreferenceWriteFailure(
                retryCount,
                block,
            )
        }
    }

    private suspend fun handlePreferenceWriteFailure(
        retryCount: Int,
        block: suspend () -> EditResult,
    ) {
        Log.e(TAG, "Failed to write preferences")

        val onRestartPref: suspend () -> Unit = {
            externalScope.launch {
                Log.i(TAG, "Trying to write preferences again")

                safePrefWriteLaunch(
                    retryCount + 1,
                    block,
                )
            }
        }

        val tooManyFailures = retryCount >= MAX_PREF_RETRY_COUNT
        snackbarDispatcher.show(
            if (tooManyFailures) {
                prefTooManyErrorsSnackbar(onAppRestart)
            } else {
                prefFailWriteSnackbar(onRestartPref)
            },
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            versionName: String,
            @Assisted("onFinished")
            onFinished: () -> Unit,
            @Assisted("onLibraryLicensesClick")
            onLibraryLicensesClick: () -> Unit,
            @Assisted("onEndpointClick")
            onEndpointClick: () -> Unit,
            @Assisted("onAppRestart")
            onAppRestart: () -> Unit,
        ): DefaultSettingsComponent
    }
}

private const val TAG = "SettingsComponent"
private const val MAX_PREF_RETRY_COUNT: Int = 3
