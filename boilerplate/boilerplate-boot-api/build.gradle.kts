// boilerplate-boot-api: 메인 API 서버 진입점 (port 8080)
// boot-conventions가 java + spring + boot 누적 적용 (BootJar 활성화 포함)
plugins {
  id("boot-conventions")
}

dependencies {
  implementation(project(":boilerplate-application"))
  implementation(libs.org.springframework.modulith.starter.jdbc)
  implementation(libs.org.springframework.modulith.events.api)
  runtimeOnly(libs.org.postgresql.postgresql)
}
