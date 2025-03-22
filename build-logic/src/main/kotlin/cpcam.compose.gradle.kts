plugins {
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("reports/compose_compiler")
    metricsDestination = layout.buildDirectory.dir("reports/compose_compiler")
}

pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
    dependencies {
        add("implementation", platform(libs.findLibrary("compose.bom").get()))

        add(
            "implementation",
            libs.findLibrary("compose.material3").get(),
        )

        add(
            "debugImplementation",
            libs.findLibrary("compose.ui.tooling").get(),
        )

        add(
            "implementation",
            libs.findLibrary("compose.ui.tooling.preview").get(),
        )
    }
}
