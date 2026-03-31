// template-adapter-output-channel: 채널 추상화 + SMS 구현
dependencies {
  implementation(project(":template-application"))

  runtimeOnly(rootProject.libs.jakarta.xml.bind.api)
}
