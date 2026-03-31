# Coding Style Rules

## 패키지 구조

베이스 패키지: `io.github.ppzxc.template`

| 레이어 | 패키지 |
|--------|--------|
| Domain | `io.github.ppzxc.template.domain` |
| Application UseCase | `io.github.ppzxc.template.application` |
| Inbound Port | `io.github.ppzxc.template.application.port.in` |
| Outbound Port | `io.github.ppzxc.template.application.port.out` |
| Web Adapter | `io.github.ppzxc.template.adapter.input.web` |
| WS Adapter | `io.github.ppzxc.template.adapter.input.ws` |
| Persist Adapter | `io.github.ppzxc.template.adapter.output.persist` |
| Common | `io.github.ppzxc.template.common` |

## 네이밍 규칙

| 타입 | 패턴 | 예시 |
|------|------|------|
| Command UseCase (Inbound Port) | `*UseCase` interface | `CreateOrderUseCase` |
| Query UseCase (Inbound Port) | `*Query` interface | `FindOrderQuery` |
| Outbound Port | `*Port` interface | `SaveOrderPort` |
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

## 코드 품질 도구

| 도구 | 명령 |
|------|------|
| Spotless (Google Java Format) | `./gradlew spotlessApply` |
| Checkstyle | `./gradlew checkstyleMain` |
| ErrorProne | 컴파일 시 자동 |
| NullAway | 컴파일 시 자동 |

코드 작성 후 반드시 `./gradlew spotlessApply` 실행.
