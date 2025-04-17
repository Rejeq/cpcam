pluginManagement {
    includeBuild("build-logic")

    repositories {
        // Chinese mirrors. Uncomment to speed up downloading or if you're in a
        // restricted region where the main repos are blocked
        // maven { url = uri("https://maven.aliyun.com/repository/public") }
        // maven { url = uri("https://maven.aliyun.com/repository/central") }
        // maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        maven { url = uri("https://maven.google.com") }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

rootProject.name = "cpcam"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")

include(":benchmarks:macro")
include(":benchmarks:micro")

include(":core:camera")
include(":core:common")
include(":core:data")
include(":core:device")
include(":core:endpoint")
include(":core:stream")
include(":core:ui")

include(":feature:about")
include(":feature:main")
include(":feature:service")
include(":feature:settings")
