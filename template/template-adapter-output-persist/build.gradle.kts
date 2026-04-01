// template-adapter-output-persist: JPA Entity, Repository, Flyway (Outbound Adapter)
dependencies {
  implementation(project(":template-application"))
  implementation(libs.org.springframework.boot.starter.data.jpa)
  implementation(libs.org.flywaydb.core)
  implementation(libs.org.flywaydb.database.postgresql)

  runtimeOnly(libs.org.postgresql)

  testImplementation(libs.org.testcontainers.postgresql)
}
