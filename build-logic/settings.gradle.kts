pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("dev.panuszewski.typesafe-conventions") version "0.10.1"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"

include("conventions")
