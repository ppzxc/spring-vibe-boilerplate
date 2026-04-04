// boilerplate-adapter-input-api: REST Controller, Spring Security (Inbound Adapter)
apply(plugin = rootProject.libs.plugins.spring.cloud.contract.get().pluginId)

configure<org.springframework.cloud.contract.verifier.plugin.ContractVerifierExtension> {
  testMode.set(org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC)
  baseClassForTests.set("io.github.ppzxc.boilerplate.adapter.input.api.ContractBaseTest")
  nameSuffixForTests.set("ContractTest")
  failOnInProgress.set(false)
  contractsDslDir.set(
    project.layout.projectDirectory.dir("src/test/resources/contracts")
  )
}

dependencies {
  implementation(project(":boilerplate-application"))
  implementation(libs.org.springframework.boot.starter.web)
  implementation(libs.org.springframework.boot.starter.security)
  implementation(libs.org.springdoc.openapi.starter.webmvc.api)

  testImplementation(libs.org.springframework.boot.starter.webmvc.test)
  testImplementation(libs.org.springframework.security.test)
  testImplementation(rootProject.libs.spring.cloud.contract.verifier)
  testImplementation(rootProject.libs.io.rest.assured.spring.mock.mvc)
}
