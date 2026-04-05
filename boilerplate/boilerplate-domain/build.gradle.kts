// boilerplate-domain: 순수 도메인 모델 — Spring/JPA 의존 금지
apply(plugin = rootProject.libs.plugins.pitest.get().pluginId)

extensions.configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
  targetClasses.set(listOf("io.github.ppzxc.boilerplate.domain.*"))
  targetTests.set(listOf("io.github.ppzxc.boilerplate.domain.*"))
  junit5PluginVersion.set("1.2.1")
  mutationThreshold.set(60)
  coverageThreshold.set(60)
  outputFormats.set(listOf("HTML", "XML"))
  avoidCallsTo.set(listOf("java.util.logging", "org.slf4j"))
  threads.set(2)
  jvmArgs.set(listOf("-XX:+EnableDynamicAgentLoading"))
}

tasks.jacocoTestCoverageVerification {
  dependsOn(tasks.jacocoTestReport)
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
  dependsOn(tasks.jacocoTestCoverageVerification)
}

dependencies {
  testImplementation(rootProject.libs.net.jqwik)
}
