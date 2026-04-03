// template-boot-api: 메인 API 서버 진입점 (port 8080)
// spring 라벨이 spring-boot-starter, spring-boot-starter-test 처리
// boot 라벨이 BootJar 활성화 처리
dependencies {
  implementation(project(":template-application-autoconfiguration"))
  implementation(project(":template-adapter-input-api"))
  implementation(project(":template-adapter-input-ws"))
  implementation(project(":template-adapter-output-persist"))
  implementation(project(":template-adapter-output-cache"))

  // database
  runtimeOnly(libs.com.h2database.h2)

  // observability (ADR-0008)
  implementation(libs.org.springframework.boot.starter.actuator)
  implementation(libs.io.micrometer.tracing.bridge.otel)
  implementation(libs.io.opentelemetry.exporter.otlp)
}
