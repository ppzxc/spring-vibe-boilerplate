// template-adapter-output-notify: Email, Push 알림
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.starter.mail)

  runtimeOnly(rootProject.libs.jakarta.xml.bind.api)
}
