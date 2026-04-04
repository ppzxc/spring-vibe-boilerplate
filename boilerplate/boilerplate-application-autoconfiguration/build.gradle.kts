// boilerplate-application-autoconfiguration: UseCase Bean 등록 (Spring AutoConfiguration)
dependencies {
  implementation(project(":boilerplate-application"))
  implementation("org.springframework:spring-tx")
  implementation("org.springframework:spring-aop")
}
