plugins {
    id("cpcam.detekt")

    id("com.mikepenz.aboutlibraries.plugin")

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

aboutLibraries {
    configPath = "config"
}

pluginManager.withPlugin("com.android.base") {
    dependencies {
        add("ksp", libs.findLibrary("hilt.android.compiler").get())
        add("implementation", libs.findLibrary("hilt.android").get())

        add(
            "androidTestImplementation",
            libs.findLibrary("hilt.android.testing").get(),
        )
    }
}
