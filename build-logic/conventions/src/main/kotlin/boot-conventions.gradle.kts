import com.google.cloud.tools.jib.gradle.JibExtension
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("spring-conventions")
  alias(libs.plugins.com.google.cloud.tools.jib)
  alias(libs.plugins.org.owasp.dependencycheck)
}

tasks.withType<BootJar>().configureEach {
  enabled = true
}

configure<JibExtension> {
  from {
    image = "eclipse-temurin:25-jre"
  }
  to {
    image = "ghcr.io/ppzxc/boilerplate"
    tags = setOf("latest", project.version.toString())
  }
  container {
    jvmFlags = listOf(
      "-XX:+UseZGC",
      "-XX:+ZGenerational",
      "-XX:MaxRAMPercentage=75.0",
      "-Djava.security.egd=file:/dev/urandom",
    )
    ports = listOf("8080")
    environment = mapOf("SPRING_PROFILES_ACTIVE" to "prod")
  }
}

// OWASP Dependency-Check: CVSS 7.0+ 시 빌드 실패 (cicd.md §1, ADR-0018)
// CI에서 ./gradlew dependencyCheckAnalyze --no-daemon 으로 명시적 실행 (check에 미포함).
configure<DependencyCheckExtension> {
  failBuildOnCVSS = 7.0f
  suppressionFile = rootProject.file("dependency-check-suppressions.xml").absolutePath
}
