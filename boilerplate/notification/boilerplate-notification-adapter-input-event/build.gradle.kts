// notification-adapter-input-event: @ApplicationModuleListener 기반 이벤트 수신 계층
plugins {
  id("spring-conventions")
}

dependencies {
  implementation(project(":boilerplate-notification-application"))
  implementation(project(":boilerplate-shared-event"))
  implementation(libs.org.springframework.modulith.events.api)
  testImplementation(project(":boilerplate-test-support"))
  testImplementation(libs.org.springframework.modulith.core)
}
