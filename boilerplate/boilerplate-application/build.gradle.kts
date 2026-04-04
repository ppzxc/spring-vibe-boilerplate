// template-application: Inbound Port + Outbound Port + UseCase 구현체 — Spring 의존 금지
apply(plugin = rootProject.libs.plugins.pitest.get().pluginId)

extensions.configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
  targetClasses.set(listOf("io.github.ppzxc.boilerplate.application.*"))
  targetTests.set(listOf("io.github.ppzxc.boilerplate.application.*"))
  junit5PluginVersion.set("1.2.1")
  mutationThreshold.set(60)
  coverageThreshold.set(60)
  outputFormats.set(listOf("HTML", "XML"))
  avoidCallsTo.set(listOf("java.util.logging", "org.slf4j"))
  threads.set(2)
  jvmArgs.set(listOf("-XX:+EnableDynamicAgentLoading"))
}

dependencies {
  api(project(":boilerplate-domain"))
  testImplementation(testFixtures(project(":boilerplate-domain")))
}
