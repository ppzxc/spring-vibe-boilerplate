# CLAUDE.md

## Source of Truth

`CLAUDE.md`, `.claude/rules/*`, `docs/decisions/*` — 이 세 경로만 프로젝트 규칙의 정본이다.
`docs/superpowers/*`는 임시 문서다. superpowers 스킬 실행 중에만 접근하고, 그 외에는 자발적으로 읽거나 검색하지 않는다.

## 이 프로젝트란?

Spring Boot 4 + Hexagonal Architecture 범용 보일러플레이트.
Java 25, Virtual Threads, PostgreSQL 18 기반.
베이스 패키지: `io.github.ppzxc.template`

## 모듈 구성 (8개)

```
template-domain                        # 순수 Java (Spring/JPA 금지)
template-application                   # 순수 Java (Spring 금지)
template-application-autoconfiguration # UseCase Bean 등록
template-adapter-input-api             # REST Controller + Security
template-adapter-input-ws              # WebSocket
template-adapter-output-persist        # JPA/Flyway
template-adapter-output-cache          # Cache
template-boot-api                      # Spring Boot 앱 (port 8080)
```

## 규칙 파일

작업 전 관련 규칙 파일을 읽는다.

| 작업 | 파일 |
|------|------|
| 아키텍처 / 레이어 작업 | `rules/architecture.md` |
| 새 모듈 추가 | `rules/module-add.md` |
| 테스트 작성 | `rules/testing.md` |
| 코드 작성 / 네이밍 | `rules/coding-style.md` |
| CI 도구 사용 | `rules/ci-tools.md` |
| ADR 작성 / 규칙 수정 | `rules/rules-maintenance.md` |
| 에러 처리 | `rules/error-handling.md` |
| 관측성 (로깅, Actuator, OTel) | `rules/observability.md` |
| API 문서화 (springdoc, Redoc, Springwolf) | `rules/api-documentation.md` |
| 컨테이너화 / 배포 | `rules/containerization.md` |
| 환경 설정 (프로파일, 환경변수) | `rules/configuration.md` |

## ADR 참조

결정의 근거가 필요하면 [`docs/decisions/README.md`](../docs/decisions/README.md) 를 참조한다.
규칙 파일의 `[ADR-NNNN]` 태그가 해당 ADR 번호를 가리킨다.
