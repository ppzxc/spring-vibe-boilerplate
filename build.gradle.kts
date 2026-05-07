plugins {
  alias(libs.plugins.com.fizzpod.lefthook)
}

allprojects {
  group = "io.github.ppzxc.boilerplate"
  version = "0.0.1"

  repositories {
    maven("https://repo.spring.io/milestone")
    mavenCentral()
  }
}
