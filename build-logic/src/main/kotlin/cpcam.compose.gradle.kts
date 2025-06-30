plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("reports/compose_compiler")
    metricsDestination = layout.buildDirectory.dir("reports/compose_compiler")
}

pluginManager.withPlugin("com.android.base") {
    dependencies {
        add("implementation", platform(libs.compose.bom))
        add("implementation", libs.compose.material3)

        add("debugImplementation", libs.compose.ui.tooling)
        add("implementation", libs.compose.ui.tooling.preview)
    }
}
