import com.diffplug.gradle.spotless.SpotlessExtension
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openrewrite.gradle.RewriteExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  idea
  `java-library`
  `java-test-fixtures`
  alias(libs.plugins.net.ltgt.errorprone)
  jacoco
  `jacoco-report-aggregation`
  alias(libs.plugins.com.diffplug.spotless)
  checkstyle
  alias(libs.plugins.org.openrewrite.rewrite)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

dependencies {
  // Spring Boot BOM via Gradle native platform —
  // SpringBootPlugin.BOM_COORDINATES 상수를 사용해 spring-boot-gradle-plugin 버전과 자동 동기화.
  // org.springframework.boot 플러그인은 apply하지 않고 클래스경로 상수만 사용 (java-conventions의 hexagonal 순수성 유지).
  val springBootBom = platform(SpringBootPlugin.BOM_COORDINATES)
  implementation(springBootBom)
  testImplementation(springBootBom)
  testFixturesImplementation(springBootBom)
  annotationProcessor(springBootBom)

  implementation(platform(libs.org.testcontainers.bom))

  jacocoAggregation(rootProject)

  errorprone(libs.com.google.errorprone.core)
  errorprone(libs.com.uber.nullaway)
  compileOnly(libs.org.jspecify)

  implementation(libs.org.slf4j.api)
  implementation(libs.org.projectlombok.lombok)
  annotationProcessor(libs.org.projectlombok.lombok)

  testRuntimeOnly(libs.org.junit.platform.launcher)
  testImplementation(libs.org.junit.jupiter)
  testImplementation(libs.org.assertj.core)
  testImplementation(libs.org.mockito.core)
  testImplementation(libs.org.mockito.junit.jupiter)
  testImplementation(libs.org.awaitility)
  testImplementation(libs.com.tngtech.archunit.junit5)

  testFixturesImplementation(libs.com.navercorp.fixture.monkey.starter)

  rewrite(libs.org.openrewrite.recipe.static.analysis)
  rewrite(libs.org.openrewrite.recipe.migrate.java)
  rewrite(libs.org.openrewrite.recipe.spring)
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
    // guava(2.41.0)·caffeine(2.43.0)이 transitive로 가져오는 구버전을
    // error_prone_core(2.49.0)와 동일 버전으로 정렬 — annotation processing class incompatibility 방지.
    // BOM(spring-boot-dependencies)이 errorprone-annotations 미관리 영역이라 force 필요.
    force(libs.com.google.errorprone.annotations)
    // postgresql JDBC가 transitive로 3.52.0을 요청 —
    // NullAway/ErrorProne 정적 분석에 필요한 type qualifier 통일.
    // BOM이 checker-qual 미관리 영역이라 force 필요.
    force(libs.org.checkerframework.checker.qual)
    // junit-platform-launcher는 spring-boot-dependencies → junit-bom 6.0.3이 자동 관리하므로 force 불필요.
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
