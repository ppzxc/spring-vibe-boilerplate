import com.diffplug.gradle.spotless.SpotlessExtension
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openrewrite.gradle.RewriteExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  idea
  `java-library`
  `java-test-fixtures`
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  id("net.ltgt.errorprone")
  jacoco
  `jacoco-report-aggregation`
  id("com.diffplug.spotless")
  checkstyle
  id("org.openrewrite.rewrite")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

// build-recipe-plugin 시절: allprojects { tasks.withType<BootJar> { enabled = false } }
// → java-conventions 적용 모듈은 기본적으로 BootJar 비활성, boot-conventions가 재활성화.
tasks.withType<BootJar>().configureEach {
  enabled = false
}

dependencies {
  implementation(platform(libs.org.testcontainers.bom))

  jacocoAggregation(rootProject)

  errorprone(libs.com.google.errorprone.core)
  errorprone(libs.com.uber.nullaway)
  compileOnly(libs.org.jspecify)

  implementation(libs.org.slf4j.api)
  implementation(libs.org.projectlombok.lombok)
  annotationProcessor(libs.org.projectlombok.lombok)

  testRuntimeOnly(libs.org.junit.platform.launcher)
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.assertj:assertj-core")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
  testImplementation(libs.org.awaitility)
  testImplementation(libs.com.tngtech.archunit.junit5)

  testFixturesImplementation(libs.com.navercorp.fixture.monkey.starter)

  "rewrite"(libs.org.openrewrite.recipe.static.analysis)
  "rewrite"(libs.org.openrewrite.recipe.migrate.java)
  "rewrite"(libs.org.openrewrite.recipe.spring)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  failOnNoDiscoveredTests = false
  systemProperty("user.timezone", "Asia/Seoul")
  systemProperty("logging.level.root", "WARN")
  jvmArgs = listOf("-XX:+EnableDynamicAgentLoading", "-Dio.netty.leakDetectionLevel=advanced")
  testLogging {
    showStandardStreams = true
    showCauses = true
    showExceptions = true
    showStackTraces = true
    exceptionFormat = TestExceptionFormat.FULL
  }
  jacoco { enabled = true }
  finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.withType<JavaCompile>().configureEach {
  options.errorprone.enabled.set(true)
  options.errorprone.disableWarningsInGeneratedCode.set(true)
  options.errorprone {
    option("NullAway:AnnotatedPackages", "io.github.ppzxc.boilerplate")
    excludedPaths = ".*/build/generated/.*"
  }
}

tasks.named<JavaCompile>("compileJava") {
  options.errorprone.error("NullAway")
}

configurations.all {
  resolutionStrategy {
    force("com.google.errorprone:error_prone_annotations:2.49.0")
    force("org.checkerframework:checker-qual:3.53.0")
    eachDependency {
      if (requested.group == "org.junit.platform" && requested.name == "junit-platform-launcher") {
        useVersion("6.0.3")
        because("Spring Boot 4 / JUnit 6 requires junit-platform-launcher 6.0.3")
      }
    }
  }
}

configure<SpotlessExtension> {
  java {
    googleJavaFormat(libs.versions.com.google.googlejavaformat.get())
    targetExclude("**/build/generated/**", "**/build/generated-sources/**")
  }
}

checkstyle {
  toolVersion = libs.versions.com.puppycrawl.tools.checkstyle.get()
  configFile = rootProject.file("config/checkstyle/checkstyle.xml")
  isIgnoreFailures = false
  maxErrors = 0
  maxWarnings = 100
}

configure<RewriteExtension> {
  configFile = rootProject.file("rewrite.yml")
  activeRecipe("io.github.ppzxc.boilerplate.CodeQuality")
}
