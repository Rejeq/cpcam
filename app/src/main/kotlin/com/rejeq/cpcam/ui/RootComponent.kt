package com.rejeq.cpcam.ui

import android.content.Context
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.BuildConfig
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.CodeVerifier
import com.rejeq.cpcam.core.common.QrScannableComponent
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.repository.DataSourceRepository
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.ui.SnackbarDispatcher
import com.rejeq.cpcam.feature.about.LibrariesComponent
import com.rejeq.cpcam.feature.about.LibraryComponent
import com.rejeq.cpcam.feature.about.LibraryState
import com.rejeq.cpcam.feature.main.DefaultMainComponent
import com.rejeq.cpcam.feature.main.MainComponent
import com.rejeq.cpcam.feature.scanner.qr.DefaultQrScannerComponent
import com.rejeq.cpcam.feature.scanner.qr.QrScannerComponent
import com.rejeq.cpcam.feature.service.ConnectionErrorComponent
import com.rejeq.cpcam.feature.service.startEndpointService
import com.rejeq.cpcam.feature.service.stopEndpointService
import com.rejeq.cpcam.feature.settings.DefaultSettingsComponent
import com.rejeq.cpcam.feature.settings.SettingsComponent
import com.rejeq.cpcam.feature.settings.endpoint.DefaultEndpointComponent
import com.rejeq.cpcam.feature.settings.endpoint.EndpointComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RootComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val mainContext: CoroutineContext,
    @ApplicationContext private val context: Context,
    appearanceRepo: AppearanceRepository,
    dataSourceRepo: DataSourceRepository,
    val snackbarDispatcher: SnackbarDispatcher,
    private val endpoint: EndpointHandler,
    private val mainFactory: DefaultMainComponent.Factory,
    private val settingsFactory: DefaultSettingsComponent.Factory,
    private val endpointSettingsFactory: DefaultEndpointComponent.Factory,
    private val qrScannerFactory: DefaultQrScannerComponent.Factory,
) : ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    private val nav = StackNavigation<Config>()
    private val dialogNavigation = SlotNavigation<DialogConfig>()

    val dialog: Value<ChildSlot<*, DialogChild>> = componentContext.childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = true,
    ) { config, childComponentContext ->
        when (config) {
            is DialogConfig.ConnectionError ->
                DialogChild.ConnectionError(
                    ConnectionErrorComponent(
                        childComponentContext,
                        config.reason,
                        onFinished = dialogNavigation::dismiss,
                        openEndpointSettings = {
                            nav.pushNew(Config.EndpointSettings)
                        },
                    ),
                )
        }
    }

    private val _stack = childStack(
        source = nav,
        initialConfiguration = Config.Main,
        serializer = Config.serializer(),
        handleBackButton = true,
        childFactory = ::child,
    )

    val stack: Value<ChildStack<*, Child>> = _stack

    init {
        dataSourceRepo.errors
            .filterNotNull()
            .map { error ->
                Log.e(TAG, "Data source error: ", error.throwable)
                error.toSnackbarState()
            }
            .onEach(snackbarDispatcher::show)
            .launchIn(scope)
    }

    val useDarkMode = appearanceRepo.themeConfig
        .stateIn(scope, SharingStarted.Eagerly, ThemeConfig.FOLLOW_SYSTEM)

    val useDynamicColor = appearanceRepo.useDynamicColor
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun onBackClicked() {
        nav.pop()
    }

    fun readyToShow() = stack.value.active.instance.component.readyToShow()

    private fun child(config: Config, context: ComponentContext): Child =
        when (config) {
            is Config.Main -> Child.Main(mainComponent(context))

            is Config.Settings -> Child.Settings(settingsComponent(context))
            is Config.EndpointSettings -> Child.EndpointSettings(
                endpointComponent(context),
            )

            is Config.QrScanner -> Child.QrScanner(
                qrScannerComponent(context, config),
            )

            is Config.Libraries -> Child.Libraries(librariesComponent(context))
            is Config.Library -> Child.Library(
                libraryComponent(context, config),
            )
        }

    private fun mainComponent(context: ComponentContext) = mainFactory.create(
        componentContext = context,
        mainContext = mainContext,
        onSettingsClick = { nav.pushNew(Config.Settings) },
        onStartEndpoint = ::startEndpoint,
        onStopEndpoint = ::stopEndpoint,
    )

    private fun settingsComponent(context: ComponentContext) =
        settingsFactory.create(
            componentContext = context,
            mainContext = mainContext,
            versionName = BuildConfig.VERSION_NAME,
            onFinished = { nav.pop() },
            onLibraryLicensesClick = { nav.pushNew(Config.Libraries) },
            onEndpointClick = { nav.pushNew(Config.EndpointSettings) },
        )

    private fun endpointComponent(context: ComponentContext) =
        endpointSettingsFactory.create(
            componentContext = context,
            mainContext = mainContext,
            onFinished = { nav.pop() },
            onQrClick = { nav.pushNew(Config.QrScanner(it)) },
        )

    private fun qrScannerComponent(
        context: ComponentContext,
        config: Config.QrScanner,
    ) = qrScannerFactory.create(
        componentContext = context,
        mainContext = mainContext,
        onFinished = {
            it?.let { qrCodeValue ->
                stack.value.backStack.reversed()
                    .firstNotNullOfOrNull {
                        it.instance.component as? QrScannableComponent
                    }
                    ?.handleQrCode(qrCodeValue)
            }

            nav.pop()
        },
        verifier = config.verifier,
    )

    private fun librariesComponent(context: ComponentContext) =
        LibrariesComponent(
            componentContext = context,
            onFinished = { nav.pop() },
            onLibraryOpen = { nav.pushNew(Config.Library(it)) },
        )

    private fun libraryComponent(
        context: ComponentContext,
        config: Config.Library,
    ) = LibraryComponent(
        componentContext = context,
        onFinished = { nav.pop() },
        state = config.state,
    )

    private fun startEndpoint() {
        scope.launch {
            val newState = async(Dispatchers.Default) {
                startEndpointService(context)
                endpoint.connect()
            }.await()

            if (newState is EndpointState.Stopped) {
                stopEndpointService(context)

                val reason = newState.reason
                if (reason != null) {
                    Log.w(TAG, "Unable to connect to endpoint: $reason")
                    showConnectionError(reason)
                } else {
                    Log.w(TAG, "Unable to connect to endpoint: Without reason")
                }
            }
        }
    }

    private fun stopEndpoint() {
        scope.launch(Dispatchers.Default) {
            stopEndpointService(context)
            endpoint.disconnect()
        }
    }

    private fun showConnectionError(reason: EndpointErrorKind) {
        dialogNavigation.activate(DialogConfig.ConnectionError(reason))
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object Settings : Config

        @Serializable
        data object EndpointSettings : Config

        @Serializable
        data class QrScanner(
            @Serializable(CodeVerifierSerializer::class)
            val verifier: CodeVerifier,
        ) : Config

        @Serializable
        data object Libraries : Config

        @Serializable
        data class Library(val state: LibraryState) : Config
    }

    @Serializable
    private sealed interface DialogConfig {
        @Serializable
        data class ConnectionError(val reason: EndpointErrorKind) : DialogConfig
    }

    sealed interface Child {
        val component: ChildComponent

        class Main(override val component: MainComponent) : Child
        class Settings(override val component: SettingsComponent) : Child
        class EndpointSettings(override val component: EndpointComponent) :
            Child
        class QrScanner(override val component: QrScannerComponent) : Child
        class Libraries(override val component: LibrariesComponent) : Child
        class Library(override val component: LibraryComponent) : Child
    }

    sealed interface DialogChild {
        class ConnectionError(val component: ConnectionErrorComponent) :
            DialogChild
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
        ): RootComponent
    }
}

private const val TAG = "RootComponent"
