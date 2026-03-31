// template-domain: 순수 도메인 모델 — Spring/JPA 의존 금지
dependencies {
  api(project(":template-common"))
  implementation(rootProject.libs.jakarta.validation.api)

  testImplementation(rootProject.libs.org.junit.jupiter)
  testImplementation(rootProject.libs.org.assertj.core)
}
