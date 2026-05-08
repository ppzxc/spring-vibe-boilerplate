import dev.panuszewski.gradle.pluginMarker

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(pluginMarker(libs.plugins.org.springframework.boot))
  implementation(pluginMarker(libs.plugins.net.ltgt.errorprone))
  implementation(pluginMarker(libs.plugins.com.diffplug.spotless))
  implementation(pluginMarker(libs.plugins.org.openrewrite.rewrite))
  implementation(pluginMarker(libs.plugins.com.google.cloud.tools.jib))
  implementation(pluginMarker(libs.plugins.org.owasp.dependencycheck))
}
