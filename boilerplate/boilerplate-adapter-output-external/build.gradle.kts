// boilerplate-adapter-output-external: External API Outbound Adapter (Resilience4j)
dependencies {
  implementation(project(":boilerplate-application"))
  implementation(libs.org.springframework.boot.starter.web)
  implementation(libs.io.github.resilience4j.circuitbreaker)
  implementation(libs.io.github.resilience4j.retry)
  implementation(libs.io.github.resilience4j.ratelimiter)
  implementation(libs.io.github.resilience4j.micrometer)
}
