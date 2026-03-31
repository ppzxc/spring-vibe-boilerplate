// template-application-autoconfiguration: UseCase Bean 등록 (Spring AutoConfiguration)
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.autoconfigure)
  annotationProcessor(rootProject.libs.org.springframework.boot.configuration.processor)
}
