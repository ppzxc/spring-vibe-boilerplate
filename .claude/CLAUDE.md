# CLAUDE.md

## 금지 사항

- `docs/superpowers/*` 경로의 문서는 읽지 않는다.

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

## ADR 참조

결정의 근거가 필요하면 `docs/decisions/NNNN-*.md` 를 직접 읽는다.
규칙 파일의 `[ADR-NNNN]` 태그가 해당 ADR 번호를 가리킨다.

| ADR | 주제 |
|-----|------|
| ADR-0001 | 모듈 레이아웃 (플랫 구조, 8개 모듈) |
| ADR-0002 | 코드 품질 도구 (Spotless, Checkstyle, ErrorProne, NullAway) |
| ADR-0003 | 아키텍처 테스트 전략 (ArchUnit) |
| ADR-0004 | CI 파이프라인 전략 (Lefthook, GitHub Actions, JaCoCo, OpenRewrite) |
