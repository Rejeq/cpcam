plugins {
    id("cpcam.detekt")

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

pluginManager.withPlugin("com.android.base") {
    dependencies {
        add("detektPlugins", libs.detekt.rules.compose)

        add("ksp", libs.hilt.android.compiler)
        add("implementation", libs.hilt.android)
        add("implementation", libs.kotlin.result)
        add("androidTestImplementation", libs.hilt.android.testing)
    }
}
