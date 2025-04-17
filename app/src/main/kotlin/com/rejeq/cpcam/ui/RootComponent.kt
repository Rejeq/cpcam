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
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.feature.about.LibrariesComponent
import com.rejeq.cpcam.feature.about.LibraryComponent
import com.rejeq.cpcam.feature.about.LibraryState
import com.rejeq.cpcam.feature.main.MainComponent
import com.rejeq.cpcam.feature.main.MainNavigation.DialogConfig
import com.rejeq.cpcam.feature.service.ConnectionErrorComponent
import com.rejeq.cpcam.feature.service.startEndpointService
import com.rejeq.cpcam.feature.service.stopEndpointService
import com.rejeq.cpcam.feature.settings.SettingsComponent
import com.rejeq.cpcam.feature.settings.endpoint.EndpointComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RootComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val mainContext: CoroutineContext,
    @ApplicationContext private val context: Context,
    private val endpoint: EndpointHandler,
    private val mainFactory: MainComponent.Factory,
    private val settingsFactory: SettingsComponent.Factory,
    private val endpointSettingsFactory: EndpointComponent.Factory,
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
            startEndpointService(context)

            val newState = endpoint.connect()
            if (newState is EndpointState.Stopped) {
                stopEndpointService(context)

                val reason = newState.reason
                if (reason != null) {
                    showConnectionError(reason)
                } else {
                    Log.w(TAG, "Unable to connect to endpoint: Without reason")
                }
            }
        }
    }

    private fun stopEndpoint() {
        scope.launch {
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
