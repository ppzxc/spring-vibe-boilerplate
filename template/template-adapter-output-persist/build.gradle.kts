// template-adapter-output-persist: Outbound Adapter (jOOQ + PostgreSQL via Flyway)
dependencies {
  implementation(project(":template-application"))
  testImplementation(libs.org.springframework.boot.starter.flyway)

  testImplementation(rootProject.libs.org.testcontainers.junit.jupiter)
  testImplementation(rootProject.libs.org.testcontainers.postgresql)
  runtimeOnly(libs.org.postgresql.postgresql)
}

tasks.matching { it.name != "jooqCodegen" && it.name != "clean" }.configureEach {
  if (this is SourceTask) {
    dependsOn(tasks.named("jooqCodegen"))
  }
}

sourceSets {
  main {
    java {
      srcDir("build/generated-sources/jooq")
    }
  }
}

jooq {
  configuration {
    generator {
      database {
        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
        properties {
          property {
            key = "scripts"
            value = "src/main/resources/db/migration"
          }
          property {
            key = "sort"
            value = "flyway"
          }
          property {
            key = "unqualifiedSchema"
            value = "none"
          }
          property {
            key = "defaultNameCase"
            value = "lower"
          }
        }
      }
      generate {
        isDeprecated = false
        isRecords = true
        isFluentSetters = true
      }
      target {
        packageName = "io.github.ppzxc.template.adapter.output.persist.jooq"
        directory = "build/generated-sources/jooq"
      }
    }
  }
}
