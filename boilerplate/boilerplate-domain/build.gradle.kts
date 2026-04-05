// boilerplate-domain: 순수 도메인 모델 — Spring/JPA 의존 금지
apply(plugin = rootProject.libs.plugins.pitest.get().pluginId)

tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        counter = "LINE"
        value = "COVEREDRATIO"
        minimum = "0.80".toBigDecimal()
      }
    }
  }
  classDirectories.setFrom(
    classDirectories.files.map { fileTree(it) { exclude("**/generated/**") } }
  )
}

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

dependencies {
  testImplementation(rootProject.libs.net.jqwik)
}
