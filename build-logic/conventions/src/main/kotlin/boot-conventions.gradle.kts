import com.google.cloud.tools.jib.gradle.JibExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("spring-conventions")
  alias(libs.plugins.com.google.cloud.tools.jib)
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
