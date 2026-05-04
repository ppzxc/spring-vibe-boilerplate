import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("java-conventions")
  alias(libs.plugins.org.springframework.boot)
}

// org.springframework.boot 플러그인이 BootJar task를 생성. boot-conventions가 활성화하므로
// 라이브러리 모듈(spring-conventions만 적용된 모듈)에서는 비활성.
tasks.withType<BootJar>().configureEach {
  enabled = false
}

dependencies {
  implementation(libs.org.springframework.boot.starter)
  implementation(libs.org.springframework.boot.starter.validation)
  implementation(libs.org.springframework.boot.starter.json)

  testImplementation(libs.org.springframework.boot.starter.test)
}
