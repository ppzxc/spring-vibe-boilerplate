// boilerplate-adapter-output-cache: Cache Adapter (Outbound Adapter) — Caffeine L1 캐시
dependencies {
  implementation(project(":boilerplate-application"))
  implementation(libs.org.springframework.boot.starter.cache)
  implementation(libs.com.github.ben.manes.caffeine)
  testImplementation(testFixtures(project(":boilerplate-domain")))
}
