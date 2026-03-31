// template-adapter-input-web: REST Controller, Security Filter
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.starter.web)
  implementation(rootProject.libs.org.springframework.boot.starter.security)
  runtimeOnly(rootProject.libs.jakarta.xml.bind.api)
  annotationProcessor(rootProject.libs.org.springframework.boot.configuration.processor)
}
