pluginManagement {
  repositories {
    maven("https://repo.spring.io/milestone")
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "template"

val modules: MutableList<Module> = mutableListOf()

fun module(name: String, path: String) {
  modules.add(Module(name, "$rootDir/$path"))
}

data class Module(val name: String, val path: String)

// ── Apps (실행 가능한 애플리케이션) ────────────────────────────────────
module(name = ":template-boot-api", path = "template/template-boot-api")

// ── Core (도메인 모듈) ──────────────────────────────────────────────────
module(name = ":template-domain", path = "template/template-domain")
module(name = ":template-application", path = "template/template-application")
module(name = ":template-application-autoconfiguration", path = "template/template-application-autoconfiguration")

// ── Adapters ───────────────────────────────────────────────────────────
module(name = ":template-adapter-input-api", path = "template/template-adapter-input-api")
module(name = ":template-adapter-input-ws", path = "template/template-adapter-input-ws")
module(name = ":template-adapter-output-persist", path = "template/template-adapter-output-persist")
module(name = ":template-adapter-output-cache", path = "template/template-adapter-output-cache")

modules.forEach {
  include(it.name)
  project(it.name).projectDir = file(it.path)
}
