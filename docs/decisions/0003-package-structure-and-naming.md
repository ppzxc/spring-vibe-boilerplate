---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 패키지 구조 및 네이밍 컨벤션

## Context and Problem Statement

Hexagonal Architecture를 채택했으므로(ADR-0001), 각 레이어의 패키지 경로와 클래스 네이밍 규칙을 통일해야 한다.
일관된 컨벤션이 없으면 도메인 경계가 무너지고, ArchUnit 규칙 작성이 어려워지며, AI와 사람 모두 코드 탐색이 비효율적이 된다.

## Decision Drivers

* AI와 사람 모두 클래스 이름만으로 역할을 파악할 수 있어야 함
* ArchUnit으로 자동 검증 가능한 패턴이어야 함
* 베이스 패키지(`io.github.ppzxc.boilerplate`) 하위에서 모듈과 패키지가 1:1 매핑

## Considered Options

* A. 접미사 기반 컨벤션 — `*UseCase`, `*Port`, `*Service`, `*Adapter`, `*JpaEntity`
* B. 패키지 기반 구분만 — 클래스 이름에 접미사 없이 패키지 경로로만 역할 구분

## Decision Outcome

Chosen option: "A. 접미사 기반 컨벤션", because
클래스명에 역할이 인코딩되어 IDE 검색과 ArchUnit 패턴 매칭이 용이하고,
AI가 코드를 생성할 때 일관된 네이밍을 적용할 수 있다.

**패키지 트리:**

```
io.github.ppzxc.boilerplate
├── domain/                                  ← boilerplate-domain
├── application/                             ← boilerplate-application
│   ├── port/input/command/   *UseCase       ← Inbound Command Port (interface)
│   ├── port/input/query/     *Query         ← Inbound Query Port (interface)
│   ├── port/output/command/  *Port          ← Outbound Command Port (interface)
│   ├── port/output/query/    *Port          ← Outbound Query Port (interface)
│   ├── port/output/shared/   *Port          ← Shared Infra Port (interface)
│   ├── service/command/      *Service       ← Command UseCase 구현체
│   └── service/query/        *Service       ← Query UseCase 구현체
├── adapter/input/api/                       ← boilerplate-adapter-input-api
├── adapter/input/ws/                        ← boilerplate-adapter-input-ws
├── adapter/output/persist/                  ← boilerplate-adapter-output-persist
└── adapter/output/cache/                    ← boilerplate-adapter-output-cache
```

**네이밍 규칙:**

| 타입 | 접미사 패턴 | 예시 |
|------|------------|------|
| Inbound Command Port | `*UseCase` interface | `CreateOrderUseCase` |
| Inbound Query Port | `*Query` interface | `FindOrderQuery` |
| Outbound Port | `*Port` interface | `SaveOrderPort`, `FindOrderPort`, `LockPort` |
| UseCase 구현체 | `*Service` | `CreateOrderService` |
| JPA Entity | `*JpaEntity` | `OrderJpaEntity` |
| JPA Repository | `*JpaRepository` | `OrderJpaRepository` |
| Outbound Adapter | `*Adapter` | `OrderPersistAdapter` |
| Controller | `*Controller` | `OrderController` |
| DTO (Request) | `*Request` | `CreateOrderRequest` |
| DTO (Response) | `*Response` | `OrderResponse` |

**Port interface 강제 규칙:**

* `..port.output..*` 패키지의 모든 타입은 반드시 interface
* `..port.input.command..*UseCase`는 반드시 interface
* `..port.input.query..*Query`는 반드시 interface

### Consequences

* Good, because 클래스명만으로 역할을 즉시 파악할 수 있다
* Good, because ArchUnit에서 접미사 패턴으로 규칙을 강제할 수 있다
* Good, because AI가 코드 생성 시 일관된 네이밍을 적용할 수 있다
* Bad, because 클래스 이름이 길어진다 (예: `OrderPersistAdapter`)

### Confirmation

ArchUnit 테스트(`ApplicationArchitectureTest`)가 Port interface 규칙을 자동 검증한다.

## Pros and Cons of the Options

### B. 패키지 기반 구분만

* Good, because 클래스 이름이 짧다
* Bad, because 같은 이름의 클래스가 여러 패키지에 존재할 수 있다 (예: `OrderRepository`가 persist와 cache 모두에)
* Bad, because IDE 검색 시 동일 이름 충돌로 혼란이 발생한다
* Bad, because ArchUnit에서 클래스명 패턴 매칭이 불가하여 패키지 경로만으로 규칙을 강제해야 한다

## More Information

* 관련 규칙: `.claude/rules/architecture.md` (패키지 구조 규칙, Port 인터페이스 규칙)
* 관련 규칙: `.claude/rules/coding-style.md` (네이밍 규칙 테이블)
* 관련 ADR: ADR-0001 (Hexagonal Architecture + CQRS 경계 분리)
