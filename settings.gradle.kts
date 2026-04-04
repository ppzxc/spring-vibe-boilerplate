pluginManagement {
  repositories {
    maven("https://repo.spring.io/milestone")
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "boilerplate"

val modules: MutableList<Module> = mutableListOf()

fun module(name: String, path: String) {
  modules.add(Module(name, "$rootDir/$path"))
}

data class Module(val name: String, val path: String)

// ── Apps (실행 가능한 애플리케이션) ────────────────────────────────────
module(name = ":boilerplate-boot-api", path = "boilerplate/boilerplate-boot-api")

// ── Core (도메인 모듈) ──────────────────────────────────────────────────
module(name = ":boilerplate-domain", path = "boilerplate/boilerplate-domain")
module(name = ":boilerplate-application", path = "boilerplate/boilerplate-application")
module(name = ":boilerplate-application-autoconfiguration", path = "boilerplate/boilerplate-application-autoconfiguration")

// ── Adapters ───────────────────────────────────────────────────────────
module(name = ":boilerplate-adapter-input-api", path = "boilerplate/boilerplate-adapter-input-api")
module(name = ":boilerplate-adapter-input-ws", path = "boilerplate/boilerplate-adapter-input-ws")
module(name = ":boilerplate-adapter-output-persist", path = "boilerplate/boilerplate-adapter-output-persist")
module(name = ":boilerplate-adapter-output-cache", path = "boilerplate/boilerplate-adapter-output-cache")
module(name = ":boilerplate-adapter-output-external", path = "boilerplate/boilerplate-adapter-output-external")

modules.forEach {
  include(it.name)
  project(it.name).projectDir = file(it.path)
}
