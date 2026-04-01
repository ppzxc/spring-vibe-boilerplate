# Coding Style Rules

## 패키지 구조

베이스 패키지: `io.github.ppzxc.template`

| 레이어 | 패키지 |
|--------|--------|
| Domain | `io.github.ppzxc.template.domain` |
| Application UseCase | `io.github.ppzxc.template.application` |
| Inbound Command Port | `io.github.ppzxc.template.application.port.input.command` |
| Inbound Query Port | `io.github.ppzxc.template.application.port.input.query` |
| Outbound Command Port | `io.github.ppzxc.template.application.port.output.command` |
| Outbound Query Port | `io.github.ppzxc.template.application.port.output.query` |
| Shared Port | `io.github.ppzxc.template.application.port.output.shared` |
| API Adapter | `io.github.ppzxc.template.adapter.input.api` |
| WS Adapter | `io.github.ppzxc.template.adapter.input.ws` |
| Persist Adapter | `io.github.ppzxc.template.adapter.output.persist` |
| Cache Adapter | `io.github.ppzxc.template.adapter.output.cache` |

## 네이밍 규칙

| 타입 | 패턴 | 예시 |
|------|------|------|
| Inbound Command Port | `*UseCase` interface | `CreateOrderUseCase` |
| Inbound Query Port | `*Query` interface | `FindOrderQuery` |
| Outbound Command Port | `*Port` interface | `SaveOrderPort` |
| Outbound Query Port | `*Port` interface | `FindOrderPort` |
| Shared Port | `*Port` interface | `LockPort` |
| UseCase 구현체 | `*Service` | `CreateOrderService` |
| JPA Entity | `*JpaEntity` | `OrderJpaEntity` |
| JPA Repository | `*JpaRepository` | `OrderJpaRepository` |
| Outbound Adapter | `*Adapter` | `OrderPersistAdapter` |
| Controller | `*Controller` | `OrderController` |
| DTO (Request) | `*Request` | `CreateOrderRequest` |
| DTO (Response) | `*Response` | `OrderResponse` |

## 코드 규칙

- Lombok 사용 허용 (모든 레이어)
- `@Nullable` / `@NonNull`: JSpecify 어노테이션 사용 (`org.jspecify`)
- 생성자 주입 방식 사용 (필드 주입 금지)
- `package-info.java` 각 모듈 루트 패키지에 필수
- DTO는 `record` 사용 권장 (불변, compact constructor에서 검증)
- 동시성 락: `ReentrantLock` 사용 (`synchronized` 금지 — Checkstyle 강제)
- 스레드 로컬: `ScopedValue` 사용 (`ThreadLocal` 금지 — Checkstyle 강제)

## 코드 품질 도구 [ADR-0005]

| 도구 | 명령 | 설명 |
|------|------|------|
| Spotless (Google Java Format) | `./gradlew spotlessApply` | 포맷 자동 수정 |
| Checkstyle | `./gradlew checkstyleMain` | 네이밍/구조 검사 |
| ErrorProne + NullAway | 컴파일 시 자동 | 버그/Null 안전성 |
| OpenRewrite | `./gradlew rewriteDryRun` | 현대화 제안 |

코드 작성 후 반드시 `./gradlew spotlessApply` 실행.
