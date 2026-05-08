// application 계층: domain만 의존 (A-1), 커버리지 80% 게이트 적용
plugins {
  id("coverage-gate-conventions")
}

dependencies {
  api(project(":boilerplate-domain"))
  testImplementation(project(":boilerplate-test-support"))
}
