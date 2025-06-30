dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.7.3"
}

rootProject.name = "build-logic"
