import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs get(): VersionCatalog =
    extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

fun Project.gitCommitHash(): String? {
    val out =
        providers.exec {
            commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        }

    return when (out.result.get().exitValue) {
        0 -> out.standardOutput.asText.getOrNull()?.trim()
        else -> null
    }
}
