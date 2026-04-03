// template-adapter-input-api: REST Controller, Spring Security (Inbound Adapter)
dependencies {
  implementation(project(":template-application"))
  implementation(libs.org.springframework.boot.starter.web)
  implementation(libs.org.springframework.boot.starter.security)
  implementation(libs.org.springdoc.openapi.starter.webmvc.api)

  testImplementation(libs.org.springframework.boot.starter.webmvc.test)
  testImplementation(libs.org.springframework.security.test)
}
