package com.rejeq.cpcam.feature.about

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.aboutlibraries.entity.Library
import com.rejeq.cpcam.core.common.ChildComponent

class LibrariesComponent(
    componentContext: ComponentContext,
    val onFinished: () -> Unit,
    val onLibraryOpen: (LibraryState) -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    fun onLibraryClick(lib: Library) = onLibraryOpen(
        LibraryState(
            name = lib.name,
            website = lib.website,
            licenses =
            lib.licenses.map { LicenseState(it) },
        ),
    )
}
