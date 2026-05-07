// 골격: 현재 사용 모듈 0개. domain/application 모듈에 80% 라인 커버리지 게이트를 적용할 때 사용.
plugins {
  id("java-conventions")
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
  dependsOn(tasks.named("jacocoTestReport"))
  violationRules {
    rule {
      limit {
        counter = "LINE"
        minimum = "0.80".toBigDecimal()
      }
    }
  }
}

tasks.named("check") {
  dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
