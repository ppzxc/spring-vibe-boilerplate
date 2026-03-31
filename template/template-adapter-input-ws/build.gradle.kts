// template-adapter-input-ws: WebSocket Handler + Protobuf (proto 라벨이 플러그인 적용)
dependencies {
  implementation(project(":template-application"))
  implementation(rootProject.libs.org.springframework.boot.starter.websocket)
  implementation(platform(rootProject.libs.com.google.protobuf.bom))
  implementation(rootProject.libs.com.google.protobuf.java)
  implementation(rootProject.libs.com.google.protobuf.java.util)

  runtimeOnly(rootProject.libs.jakarta.xml.bind.api)
}

protobuf {
  protoc {
    artifact = rootProject.libs.com.google.protobuf.protoc.get().toString()
  }
}
