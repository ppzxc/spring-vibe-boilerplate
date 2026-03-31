// template-adapter-output-persist: JPA Entity, Repository 구현, Flyway
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.starter.data.jpa)
  implementation(rootProject.libs.org.springframework.boot.starter.data.jdbc)
  implementation(rootProject.libs.org.flywaydb.core)
  implementation(rootProject.libs.org.flywaydb.database.postgresql)
  implementation(rootProject.libs.org.springframework.boot.flyway)
  runtimeOnly(rootProject.libs.org.postgresql)
  runtimeOnly(rootProject.libs.jakarta.xml.bind.api)
  implementation(rootProject.libs.org.mapstruct)
  annotationProcessor(rootProject.libs.org.mapstruct.processor)

  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}
