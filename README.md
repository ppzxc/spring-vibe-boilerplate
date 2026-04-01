# java-spring-template

Spring Boot 4 + Hexagonal Architecture boilerplate template.

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 25 |
| Spring Boot | 4.0.3 |
| Gradle | 9.3.1 (Kotlin DSL) |
| PostgreSQL | 18 |
| Flyway | 12.0.1 |
| Virtual Threads | Enabled |

## Module Structure

```
template/
  template-boot-api/                       # Port 8080 - Main API
  template-domain/                         # Pure domain (NO Spring, NO JPA)
  template-application/                    # Ports + UseCases (NO Spring)
  template-application-autoconfiguration/  # UseCase Bean registration
  template-adapter-input-api/              # REST + Security
  template-adapter-input-ws/              # WebSocket
  template-adapter-output-persist/         # JPA + Flyway
  template-adapter-output-cache/           # Cache
```

## Architecture

Hexagonal Architecture (Ports & Adapters):

```
Inbound Adapters       Application Core       Outbound Adapters
(input-api)    ──>    (application)    ──>   (output-persist)
(input-ws)            (domain)               (output-cache)
```

### Dependency Rules (enforced by ArchUnit)

- `template-domain`: NO Spring, NO JPA dependencies
- `template-application`: NO Spring dependencies
- Outbound ports must be interfaces
- Query services cannot depend on Command ports

## Quick Start

```bash
# Build
./gradlew build

# Run architecture tests
./gradlew :template-domain:test :template-application:test

# Code quality
./gradlew spotlessCheck checkstyleMain

# Run API server
./gradlew :template-boot-api:bootRun
```

## Package Convention

Base package: `io.github.ppzxc.template`

| Layer | Package |
|-------|---------|
| Domain | `io.github.ppzxc.template.domain` |
| Application | `io.github.ppzxc.template.application` |
| API Adapter | `io.github.ppzxc.template.adapter.input.api` |
| WS Adapter | `io.github.ppzxc.template.adapter.input.ws` |
| Persist Adapter | `io.github.ppzxc.template.adapter.output.persist` |
| Cache Adapter | `io.github.ppzxc.template.adapter.output.cache` |

## Adding a New Domain

1. Add domain model to `template-domain`
2. Add port interfaces to `template-application/port/`
3. Add UseCase interfaces and implementations to `template-application/`
4. Register UseCase beans in `ApplicationAutoConfiguration`
5. Add JPA entities and repositories to `template-adapter-output-persist`
6. Add controllers to `template-adapter-input-api`
7. Add Flyway migration to `template-adapter-output-persist/src/main/resources/db/migration/`
