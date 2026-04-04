// template-application: Inbound Port + Outbound Port + UseCase 구현체 — Spring 의존 금지
apply(plugin = rootProject.libs.plugins.pitest.get().pluginId)

extensions.configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
  targetClasses.set(listOf("io.github.ppzxc.template.application.*"))
  targetTests.set(listOf("io.github.ppzxc.template.application.*"))
  junit5PluginVersion.set("1.2.1")
  mutationThreshold.set(60)
  coverageThreshold.set(60)
  outputFormats.set(listOf("HTML", "XML"))
  avoidCallsTo.set(listOf("java.util.logging", "org.slf4j"))
  threads.set(2)
  jvmArgs.set(listOf("-XX:+EnableDynamicAgentLoading"))
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform" && requested.name == "junit-platform-launcher") {
      useVersion("6.0.3")
      because("Spring Boot 4 / JUnit 6 requires junit-platform-launcher 6.0.3")
    }
  }
}

dependencies {
  api(project(":template-domain"))
  testImplementation(testFixtures(project(":template-domain")))
}
