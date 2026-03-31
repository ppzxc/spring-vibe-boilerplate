// template-boot-api: 메인 API 서버 진입점 (port 8080)
// spring 라벨이 spring-boot-starter, spring-boot-starter-test 처리
// boot 라벨이 BootJar 활성화 처리
dependencies {
  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

  implementation(project(":template-application-autoconfiguration"))
  implementation(project(":template-adapter-input-web"))
  implementation(project(":template-adapter-input-ws"))
  implementation(project(":template-adapter-output-persist"))
  implementation(project(":template-adapter-output-cache"))
  implementation(project(":template-adapter-output-channel"))
  implementation(project(":template-adapter-output-notify"))
}
