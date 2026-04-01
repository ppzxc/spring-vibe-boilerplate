// template-adapter-output-cache: Cache Adapter (Outbound Adapter)
dependencies {
  implementation(project(":template-application"))
  implementation(libs.org.springframework.boot.starter.cache)
}
