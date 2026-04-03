// template-application-autoconfiguration: UseCase Bean 등록 (Spring AutoConfiguration)
dependencies {
  implementation(project(":template-application"))
  implementation("org.springframework:spring-tx")
  implementation("org.springframework:spring-aop")
}
