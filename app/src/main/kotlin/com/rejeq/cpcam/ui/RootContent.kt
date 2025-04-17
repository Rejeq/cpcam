package com.rejeq.cpcam.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.rejeq.cpcam.feature.about.LibrariesContent
import com.rejeq.cpcam.feature.about.LibraryContent
import com.rejeq.cpcam.feature.main.MainContent
import com.rejeq.cpcam.feature.service.ConnectionErrorContent
import com.rejeq.cpcam.feature.settings.SettingsContent
import com.rejeq.cpcam.feature.settings.endpoint.EndpointContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier,
        stack = component.stack,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(slide()),
            onBack = component::onBackClicked,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val child = it.instance) {
                is RootComponent.Child.Main -> MainContent(
                    component = child.component,
                )

                is RootComponent.Child.Settings -> SettingsContent(
                    component = child.component,
                )

                is RootComponent.Child.EndpointSettings -> EndpointContent(
                    component = child.component,
                )

                is RootComponent.Child.Libraries -> LibrariesContent(
                    component = child.component,
                )

                is RootComponent.Child.Library -> LibraryContent(
                    component = child.component,
                )
            }
        }

        val dialog = component.dialog.subscribeAsState().value
        dialog.child?.instance?.let {
            when (it) {
                is RootComponent.DialogChild.ConnectionError ->
                    ConnectionErrorContent(it.component)
            }
        }
    }
}
