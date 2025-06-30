import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.kotlin.dsl.add

plugins {
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom("${project.rootDir}/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = ProjectConfig.JVM_TARGET

    reports {
        val detektDir = layout.buildDirectory.dir("reports/detekt")

        md.required.set(true)
        md.outputLocation.set(detektDir.map { it.file("detekt.md") })

        html.required.set(true)
        html.outputLocation.set(detektDir.map { it.file("detekt.html") })

        sarif.required.set(false)
        txt.required.set(false)
        xml.required.set(false)
    }
}

tasks.withType<DetektCreateBaselineTask> {
    jvmTarget = ProjectConfig.JVM_TARGET
}

