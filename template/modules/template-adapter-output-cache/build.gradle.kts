// template-adapter-output-cache: Caffeine (Phase 1) / Redis (Phase 2)
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.starter.cache)
  implementation(rootProject.libs.com.github.ben.manes.caffeine)
}
