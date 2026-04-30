plugins {
  id("java-conventions")
}

dependencies {
  implementation(libs.org.springframework.boot.starter)
  implementation(libs.org.springframework.boot.starter.validation)
  implementation(libs.org.springframework.boot.starter.json)

  testImplementation(libs.org.springframework.boot.starter.test)
}
