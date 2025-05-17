plugins {
    id("cpcam.detekt")

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

pluginManager.withPlugin("com.android.base") {
    dependencies {
        add("ksp", libs.findLibrary("hilt.android.compiler").get())
        add("implementation", libs.findLibrary("hilt.android").get())
        add("implementation", libs.findLibrary("kotlin.result").get())

        add(
            "androidTestImplementation",
            libs.findLibrary("hilt.android.testing").get(),
        )
    }
}
