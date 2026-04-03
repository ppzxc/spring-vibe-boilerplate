// template-adapter-output-persist: Outbound Adapter (jOOQ + H2)
dependencies {
  implementation(project(":template-application"))
  runtimeOnly(libs.com.h2database.h2)
}
