// boilerplate-test-support: DomainTestBase + AdapterTestBase (Testcontainers Singleton)
// main 소스에서 @SpringBootTest / @Testcontainers 사용하므로 implementation 스코프 사용
plugins {
  id("java-conventions")
}

dependencies {
  api(platform(libs.org.testcontainers.bom))
  api(libs.org.springframework.boot.starter.test)
  api(libs.org.testcontainers.junit.jupiter)
  api(libs.org.testcontainers.postgresql)
}
