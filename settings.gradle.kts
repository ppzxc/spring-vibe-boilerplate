pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "boilerplate"

fun module(name: String, path: String) {
  include(name)
  project(name).projectDir = file("$rootDir/$path")
}

// ── Core ───────────────────────────────────────────────────────────────
module(name = ":boilerplate-domain", path = "boilerplate/boilerplate-domain")
module(name = ":boilerplate-application", path = "boilerplate/boilerplate-application")

// ── Apps (실행 가능한 애플리케이션) ────────────────────────────────────
module(name = ":boilerplate-boot-api", path = "boilerplate/boilerplate-boot-api")
