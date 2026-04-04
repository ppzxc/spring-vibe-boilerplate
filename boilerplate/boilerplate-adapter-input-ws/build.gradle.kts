// template-adapter-input-ws: WebSocket Handler (Inbound Adapter)
dependencies {
  implementation(project(":boilerplate-application"))
  implementation(libs.org.springframework.boot.starter.websocket)
  implementation(libs.io.github.springwolf.stomp)

  runtimeOnly(libs.io.github.springwolf.ui)
}
