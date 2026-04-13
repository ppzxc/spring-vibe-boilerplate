rootProject.name = "boilerplate"

val modules: MutableList<Module> = mutableListOf()

fun module(name: String, path: String) {
  modules.add(Module(name, "$rootDir/$path"))
}

data class Module(val name: String, val path: String)

// ── Core ───────────────────────────────────────────────────────────────
module(name = ":boilerplate-domain", path = "boilerplate/boilerplate-domain")
module(name = ":boilerplate-application", path = "boilerplate/boilerplate-application")

// ── Apps (실행 가능한 애플리케이션) ────────────────────────────────────
module(name = ":boilerplate-boot-api", path = "boilerplate/boilerplate-boot-api")

modules.forEach {
  include(it.name)
  project(it.name).projectDir = file(it.path)
}
