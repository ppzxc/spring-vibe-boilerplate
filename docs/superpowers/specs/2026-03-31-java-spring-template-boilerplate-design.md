# java-spring-template Boilerplate Design

**Date**: 2026-03-31
**Status**: Implemented

## Context

via 프로젝트(`/home/ppzxc/projects/via`)의 hexagonal architecture 스캐폴딩을 기반으로
재사용 가능한 Spring Boot boilerplate 템플릿을 만든다.

도메인 코드, 포트, 테스트(ArchUnit 제외)는 모두 제거하고 순수 인프라/빌드 구조만 유지한다.

## Decisions

### 포함
- Gradle 멀티모듈 빌드 (`com.linecorp.build-recipe-plugin` label 기반)
- 11개 모듈 구조 (via-web 프론트엔드 제외)
- 코드 품질: Spotless, Checkstyle, Error Prone, NullAway, OpenRewrite, JaCoCo
- CI/CD: GitHub Actions 4 jobs, lefthook git hooks
- ArchUnit 아키텍처 테스트 (domain, application 레이어)
- AutoConfiguration 패턴 (각 어댑터 모듈)
- ApiVersionFilter + SecurityConfig (인프라 코드)
- Flyway 기초 마이그레이션 (V1__init.sql)

### 제외
- 모든 도메인 코드 (DomainException, ErrorCode, DomainEvent 등)
- 모든 포트 (TransactionPort, EventPublisherPort 등)
- 모든 서비스/어댑터 구현체
- 모든 테스트 (ArchUnit 제외)
- 프론트엔드 (via-web)
- GlobalExceptionHandler (DomainException 의존)
- JWT/BCrypt/Sha256 어댑터 (도메인 포트 구현체)

## Transformation Rules

| Source | Target |
|--------|--------|
| `kr.nanoit.via` | `io.github.ppzxc.template` |
| `via-*` (modules) | `template-*` |
| `ViaApiApplication` | `TemplateApiApplication` |
| `via.` (property prefix) | `template.` |
| DB name `via` | `template` |

## Module Structure

```
template/
  apps/
    template-boot-api/          # label: java, spring, boot
    template-boot-admin/        # label: java, spring, boot
  modules/
    template-domain/            # label: java
    template-application/       # label: java
    template-application-autoconfiguration/  # label: java, spring
    template-adapter-input-web/    # label: java, spring
    template-adapter-input-ws/     # label: java, spring, proto
    template-adapter-output-persist/ # label: java, spring
    template-adapter-output-cache/   # label: java, spring
    template-adapter-output-channel/ # label: java, spring
    template-adapter-output-notify/  # label: java, spring
  libs/
    template-common/            # label: java, spring
```

## Verification Results

- `./gradlew compileJava` — BUILD SUCCESSFUL
- `./gradlew :template-domain:test :template-application:test` — BUILD SUCCESSFUL (ArchUnit)
- `./gradlew spotlessCheck checkstyleMain` — BUILD SUCCESSFUL
