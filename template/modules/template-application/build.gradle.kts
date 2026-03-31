// template-application: Inbound Port + Outbound Port + UseCase 구현체 — Spring 의존 금지
dependencies {
  api(project(":template-domain"))

  // spring-boot-starter-test가 없는 순수 java 모듈 — Mockito 직접 추가
  testImplementation(rootProject.libs.org.junit.jupiter)
  testImplementation(rootProject.libs.org.assertj.core)
  testImplementation(rootProject.libs.org.mockito.core)
  testImplementation(rootProject.libs.org.mockito.junit.jupiter)
}
