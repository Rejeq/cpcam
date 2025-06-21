package com.rejeq.cpcam.ui

import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.polymorphicSerializer
import com.rejeq.cpcam.core.common.CodeVerifier
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.ObsQrVerifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

// TODO: Compile time resoloving
@OptIn(ExperimentalSerializationApi::class, ExperimentalStateKeeperApi::class)
object CodeVerifierSerializer :
    KSerializer<CodeVerifier> by polymorphicSerializer(
        baseClass = CodeVerifier::class,
        module = SerializersModule {
            polymorphic(
                CodeVerifier::class,
                ObsQrVerifier::class,
                ObsQrVerifier.serializer(),
            )
        },
    )
