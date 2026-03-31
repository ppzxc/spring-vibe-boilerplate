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
module(name = ":template-boot-api", path = "template/apps/template-boot-api")
module(name = ":template-boot-admin", path = "template/apps/template-boot-admin")

// ── Modules (도메인 모듈) ──────────────────────────────────────────────
module(name = ":template-domain", path = "template/modules/template-domain")
module(name = ":template-application", path = "template/modules/template-application")
module(name = ":template-application-autoconfiguration", path = "template/modules/template-application-autoconfiguration")
module(name = ":template-adapter-input-web", path = "template/modules/template-adapter-input-web")
module(name = ":template-adapter-input-ws", path = "template/modules/template-adapter-input-ws")
module(name = ":template-adapter-output-persist", path = "template/modules/template-adapter-output-persist")
module(name = ":template-adapter-output-cache", path = "template/modules/template-adapter-output-cache")
module(name = ":template-adapter-output-channel", path = "template/modules/template-adapter-output-channel")
module(name = ":template-adapter-output-notify", path = "template/modules/template-adapter-output-notify")

// ── Libs (공용 라이브러리) ─────────────────────────────────────────────
module(name = ":template-common", path = "template/libs/template-common")

modules.forEach {
  include(it.name)
  project(it.name).projectDir = file(it.path)
}
