package com.rejeq.cpcam.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.BuildConfig
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.feature.about.LibrariesComponent
import com.rejeq.cpcam.feature.about.LibraryComponent
import com.rejeq.cpcam.feature.about.LibraryState
import com.rejeq.cpcam.feature.main.MainComponent
import com.rejeq.cpcam.feature.settings.SettingsComponent
import com.rejeq.cpcam.feature.settings.endpoint.EndpointComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.serialization.Serializable

class RootComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val mainContext: CoroutineContext,
    private val mainFactory: MainComponent.Factory,
    private val settingsFactory: SettingsComponent.Factory,
    private val endpointSettingsFactory: EndpointComponent.Factory,
) : ComponentContext by componentContext {
    private val nav = StackNavigation<Config>()

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
        openEndpointSettings = { nav.pushNew(Config.EndpointSettings) },
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

    sealed interface Child {
        val component: ChildComponent

        class Main(override val component: MainComponent) : Child
        class Settings(override val component: SettingsComponent) : Child
        class EndpointSettings(override val component: EndpointComponent) :
            Child
        class Libraries(override val component: LibrariesComponent) : Child
        class Library(override val component: LibraryComponent) : Child
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
        ): RootComponent
    }
}
