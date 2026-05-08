// notification-configuration: Bean 등록, TX 프록시
plugins {
  id("spring-conventions")
}

dependencies {
  implementation(project(":boilerplate-notification-domain"))
  implementation(project(":boilerplate-notification-application"))
  implementation(project(":boilerplate-notification-adapter-input-event"))
  implementation(project(":boilerplate-notification-adapter-output-persist"))
  implementation(project(":boilerplate-shared-event"))
  implementation(libs.org.springframework.modulith.events.api)
}
