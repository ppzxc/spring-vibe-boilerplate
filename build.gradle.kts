import com.diffplug.gradle.spotless.SpotlessExtension
import com.linecorp.support.project.multi.recipe.configureByLabel
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openrewrite.gradle.RewriteExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  idea
  java
  `java-test-fixtures`
  alias(libs.plugins.org.springframework.boot)
  alias(libs.plugins.io.spring.dependency.management)
  alias(libs.plugins.com.linecorp.build.recipe.plugin)
  alias(libs.plugins.net.ltgt.errorprone)
  alias(libs.plugins.com.google.protobuf) apply false
  alias(libs.plugins.com.diffplug.spotless) apply false
  alias(libs.plugins.org.openrewrite.rewrite) apply false
  alias(libs.plugins.com.fizzpod.lefthook)
  jacoco
  `jacoco-report-aggregation`
}

allprojects {
  group = "io.github.ppzxc.template"
  version = "0.0.1"

  repositories {
    maven("https://repo.spring.io/milestone")
    mavenCentral()
  }

  tasks.withType<BootJar> {
    enabled = false
  }
}

// ── java 라벨: Java 25 + 공통 빌드 설정 ─────────────────────────────
configureByLabel("java") {
  apply(plugin = "idea")
  apply(plugin = "java-library")
  apply(plugin = "java-test-fixtures")
  apply(plugin = "org.springframework.boot")
  apply(plugin = "io.spring.dependency-management")
  apply(plugin = "net.ltgt.errorprone")
  apply(plugin = "jacoco")
  apply(plugin = "jacoco-report-aggregation")

  java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

  dependencies {
    implementation(platform(rootProject.libs.org.testcontainers.bom))

    jacocoAggregation(rootProject)

    errorprone(rootProject.libs.com.google.errorprone.core)
    errorprone(rootProject.libs.com.uber.nullaway)
    compileOnly(rootProject.libs.org.jspecify)

    implementation(rootProject.libs.org.slf4j.api)
    implementation(rootProject.libs.org.projectlombok.lombok)
    annotationProcessor(rootProject.libs.org.projectlombok.lombok)

    testRuntimeOnly(rootProject.libs.org.junit.platform.launcher)
    testImplementation(rootProject.libs.org.awaitility)
    testImplementation(rootProject.libs.com.tngtech.archunit.junit5)

    testFixturesImplementation(rootProject.libs.com.navercorp.fixture.monkey.starter)
  }

  tasks.withType<Test> {
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
    finalizedBy(tasks.jacocoTestReport)
  }

  tasks.withType<JavaCompile>().configureEach {
    options.errorprone.isEnabled.set(true)
    options.errorprone.disableWarningsInGeneratedCode.set(true)
    options.errorprone {
      option("NullAway:AnnotatedPackages", "io.github.ppzxc.template")
      excludedPaths = ".*/build/generated/.*"
    }
  }

  tasks.compileJava {
    options.errorprone.error("NullAway")
  }

  configurations.all {
    resolutionStrategy {
      force("com.google.errorprone:error_prone_annotations:2.36.0")
      force("org.checkerframework:checker-qual:3.48.4")
    }
  }

  val versionCatalog = rootProject.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

  // ── Spotless: Google Java Format ──────────────────────────────────
  apply(plugin = "com.diffplug.spotless")
  configure<SpotlessExtension> {
    java {
      googleJavaFormat(
        versionCatalog.findVersion("com-google-googlejavaformat").get().requiredVersion
      )
      targetExclude("**/build/generated/**")
    }
  }

  // ── Checkstyle: 네이밍 + 금지 패턴 ───────────────────────────────
  apply(plugin = "checkstyle")
  configure<CheckstyleExtension> {
    toolVersion = versionCatalog.findVersion("com-puppycrawl-tools-checkstyle").get().requiredVersion
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxErrors = 0
    maxWarnings = 100
  }

  // ── OpenRewrite: 코드 품질 레시피 ────────────────────────────────
  apply(plugin = "org.openrewrite.rewrite")
  configure<RewriteExtension> {
    configFile = rootProject.file("rewrite.yml")
    activeRecipe("io.github.ppzxc.template.CodeQuality")
  }
  dependencies {
    add("rewrite", versionCatalog.findLibrary("org-openrewrite-recipe-static-analysis").get())
    add("rewrite", versionCatalog.findLibrary("org-openrewrite-recipe-migrate-java").get())
    add("rewrite", versionCatalog.findLibrary("org-openrewrite-recipe-spring").get())
  }
}

// ── spring 라벨: Spring Boot 공통 의존성 ────────────────────────────
configureByLabel("spring") {
  apply(plugin = "org.springframework.boot")
  apply(plugin = "io.spring.dependency-management")

  dependencies {
    implementation(rootProject.libs.org.springframework.boot.starter)
    implementation(rootProject.libs.org.springframework.boot.starter.validation)
    implementation(rootProject.libs.org.springframework.boot.starter.json)

    testImplementation(rootProject.libs.org.springframework.boot.starter.test)
  }
}

// ── boot 라벨: BootJar 활성화 ───────────────────────────────────────
configureByLabel("boot") {
  tasks.withType<BootJar> {
    enabled = true
  }
}

// ── proto 라벨: Protobuf 플러그인 적용 ──────────────────────────────
// protobuf { } 블록은 각 모듈 build.gradle.kts에서 설정 (Kotlin DSL 컴파일 타임 제약)
configureByLabel("proto") {
  apply(plugin = "com.google.protobuf")
}
