package com.rejeq.cpcam.feature.about

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.ui.compose.m3.util.htmlReadyLicenseContent
import com.rejeq.cpcam.core.common.ChildComponent
import kotlinx.serialization.Serializable

@Serializable
data class LicenseState(
    val name: String,
    val url: String?,
    val htmlContent: String?,
) {
    constructor(license: License) :
        this(license.name, license.url, license.htmlReadyLicenseContent)
}

@Serializable
data class LibraryState(
    val name: String,
    val website: String?,
    val licenses: List<LicenseState>,
)

class LibraryComponent(
    componentContext: ComponentContext,
    val state: LibraryState,
    val onFinished: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext
