// audit-configuration: Bean 등록, TX 프록시
plugins {
  id("spring-conventions")
}

dependencies {
  implementation(project(":boilerplate-audit-domain"))
  implementation(project(":boilerplate-audit-application"))
  implementation(project(":boilerplate-audit-adapter-input-event"))
  implementation(project(":boilerplate-audit-adapter-output-persist"))
  implementation(project(":boilerplate-shared-event"))
  implementation(libs.org.springframework.modulith.events.api)
}
