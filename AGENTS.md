# AGENTS.md

> Universal project instructions — read by Claude Code, Gemini CLI, GitHub Copilot, OpenAI Codex, Cursor, Windsurf, and other AI coding tools.

## Project Overview

Spring Boot 4 + Hexagonal Architecture 범용 보일러플레이트.
Java 25, Virtual Threads 기반.
베이스 패키지: `io.github.ppzxc.boilerplate`

## Modules (8)

```
boilerplate-domain                        # 순수 Java (Spring/JPA 금지)
boilerplate-application                   # 순수 Java (Spring 금지)
boilerplate-application-autoconfiguration # UseCase Bean 등록
boilerplate-adapter-input-api             # REST Controller + Security
boilerplate-adapter-input-ws              # WebSocket
boilerplate-adapter-output-persist        # Outbound Adapter (영속화 기술 선택)
boilerplate-adapter-output-cache          # Cache
boilerplate-boot-api                      # Spring Boot 앱 (port 8080)
```

## Quick Commands

```bash
./gradlew spotlessApply   # 포맷 수정 (코드 작성 후 필수)
./gradlew compileJava     # 컴파일 (ErrorProne + NullAway 포함)
./gradlew test            # 전체 테스트
./gradlew bootRun         # 로컬 실행
```

**코드 작성 후 반드시 `./gradlew spotlessApply` 실행.**

## Architecture Rules

- Hexagonal Architecture: domain → application → adapter 단방향 의존
- `boilerplate-domain`, `boilerplate-application`: 순수 Java만 (Spring/JPA 금지)
- adapter 간 상호 의존 전면 금지 — adapter 통신은 application 포트 경유
- 비즈니스 로직은 domain/application 레이어에서만 구현
- 트랜잭션 경계는 `boilerplate-application-autoconfiguration`에서 데코레이터 적용

## Coding Conventions

- Lombok 사용 허용 (모든 레이어)
- Null 안전성: JSpecify `@Nullable` / `@NonNull` (`org.jspecify`)
- DTO는 `record` 사용 (불변, compact constructor에서 검증)
- 생성자 주입 방식 사용 (필드 주입 금지)
- `synchronized` 금지 → `ReentrantLock` 사용
- `ThreadLocal` 금지 → `ScopedValue` 사용

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Inbound Command Port | `*UseCase` interface | `CreateOrderUseCase` |
| Inbound Query Port | `*Query` interface | `FindOrderQuery` |
| Outbound Port | `*Port` interface | `SaveOrderPort` |
| UseCase 구현체 | `*Service` | `CreateOrderService` |
| Controller | `*Controller` | `OrderController` |
| DTO Request/Response | `*Request` / `*Response` | `CreateOrderRequest` |
| JPA Entity | `*JpaEntity` | `OrderJpaEntity` |
| Outbound Adapter | `*Adapter` | `OrderPersistAdapter` |

## Rules Reference

상세 규칙은 `.claude/rules/` 디렉토리 참조. ADR은 `docs/decisions/` 참조.

| Task | Rule File |
|------|-----------|
| 아키텍처 / 레이어 | `.claude/rules/architecture.md` |
| 새 모듈 추가 | `.claude/rules/module-add.md` |
| 테스트 작성 | `.claude/rules/testing.md` |
| 코드 작성 / 네이밍 | `.claude/rules/coding-style.md` |
| CI 도구 | `.claude/rules/ci-tools.md` |
| 에러 처리 | `.claude/rules/error-handling.md` |
| 관측성 (로깅, OTel) | `.claude/rules/observability.md` |
| API 문서화 | `.claude/rules/api-documentation.md` |
| 컨테이너화 / 배포 | `.claude/rules/containerization.md` |
| 환경 설정 | `.claude/rules/configuration.md` |
| Bean 조립 | `.claude/rules/assembly.md` |
| 멀티테넌시 | `.claude/rules/multi-tenancy.md` |
| ADR 작성 / 규칙 수정 | `.claude/rules/rules-maintenance.md` |
