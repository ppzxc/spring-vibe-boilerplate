import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("spring-conventions")
}

tasks.withType<BootJar>().configureEach {
  enabled = true
}
