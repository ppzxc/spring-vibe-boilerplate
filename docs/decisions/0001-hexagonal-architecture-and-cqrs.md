---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# Hexagonal Architecture + CQRS 경계 분리 채택

## Context and Problem Statement

Spring Boot 멀티모듈 보일러플레이트의 아키텍처 패턴을 결정해야 한다.
비즈니스 로직이 프레임워크/인프라에 결합되면 테스트가 어렵고, 인프라 교체 시 비즈니스 로직까지 수정해야 한다.
또한 읽기/쓰기 로직이 혼재하면 복잡도가 급증하고, 독립적인 확장이 어렵다.

## Decision Drivers

* 비즈니스 로직의 프레임워크 독립성
* 외부 의존성 격리를 통한 테스트 용이성
* adapter 교체만으로 인프라 변경 가능한 구조
* 보일러플레이트로서의 범용 적용 가능성
* 읽기/쓰기 로직의 독립적 확장 기반

## Considered Options

* A. Hexagonal Architecture (Ports & Adapters) + CQRS 경계 분리
* B. Layered Architecture (Controller → Service → Repository)
* C. Clean Architecture (Uncle Bob 동심원 레이어)

## Decision Outcome

Chosen option: "A. Hexagonal Architecture + CQRS 경계 분리", because
Port interface를 통해 외부 의존성을 격리하여 테스트가 용이하고,
domain/application이 프레임워크에 오염되지 않아 인프라 교체에도 코어 로직이 불변하며,
CQRS 경계를 ArchUnit으로 강제하여 읽기/쓰기 로직의 독립성을 보장한다.

**Hexagonal 채택 이유:**

* 테스트 용이성 — Port interface를 통해 외부 의존성 격리. domain/application 단위 테스트에 Spring Context 불필요
* 도메인 보호 — domain/application이 Spring/JPA에 오염되지 않아 프레임워크 교체에도 코어 로직 불변
* 인프라 교체 자유도 — adapter 교체만으로 DB, 캐시, 메시징 등 기술 스택 변경 가능
* 보일러플레이트 범용성 — adapter를 추가/교체하는 구조라 다양한 프로젝트에 적용 가능

**CQRS 경계 분리 이유:**

* 읽기/쓰기 로직 분리로 각각의 복잡도를 독립 관리
* query service가 command port에 의존하면 조회에서 쓰기 부수효과 발생 가능 — ArchUnit으로 원천 차단
* 향후 읽기 전용 replica나 캐시 최적화 시 변경 범위 최소화
* 보일러플레이트로서 구조적 가이드를 자동화(ArchUnit)하여 fork 사용자의 실수 방지

**의존성 방향:**

```
Inbound Adapter ──→ [Port] ──→ Application(UseCase) ──→ [Port] ──→ Outbound Adapter
                                       │
                                       ↓
                                Domain (순수 Java)
```

### Consequences

* Good, because domain/application 단위 테스트에 Spring Context가 불필요하여 테스트 속도와 격리성이 높다
* Good, because adapter를 교체/추가하는 것만으로 인프라 기술 스택을 변경할 수 있다
* Good, because CQRS 경계를 ArchUnit으로 자동 검증하여 읽기/쓰기 로직의 독립성을 보장한다
* Bad, because adapter 간 직접 통신이 불가하여 반드시 application port를 경유해야 한다
* Bad, because Port/Adapter 보일러플레이트 코드가 Layered 대비 증가한다

### Confirmation

ArchUnit 테스트가 의존성 방향과 CQRS 경계를 자동 검증한다:

* `DomainArchitectureTest` — domain 레이어의 Spring/JPA 의존 금지
* `ApplicationArchitectureTest` — application 레이어의 Spring 의존 금지, query→command port 의존 금지

## Pros and Cons of the Options

### B. Layered Architecture

* Good, because 단순하고 학습 곡선이 낮다
* Good, because 소규모 프로젝트에서 빠르게 시작할 수 있다
* Bad, because 비즈니스 로직이 프레임워크(Spring/JPA)에 결합된다
* Bad, because 인프라 교체 시 전 레이어 수정이 필요하다
* Bad, because 단위 테스트에 Spring Context가 필요하여 테스트 속도가 느리다

### C. Clean Architecture

* Good, because Hexagonal과 원칙이 유사하며 의존성 규칙이 명확하다
* Good, because Entity/UseCase/Interface Adapter/Framework 레이어 구분이 엄격하다
* Bad, because 동심원 4레이어가 Hexagonal의 3레이어보다 복잡하여 보일러플레이트로는 과도하다
* Bad, because Hexagonal 대비 실질적 이점이 크지 않으면서 구조적 오버헤드가 증가한다

## More Information

* 관련 규칙: `.claude/rules/architecture.md` (의존성 방향 규칙, 금지 규칙)
* 관련 ADR: ADR-0006 (패키지 구조 및 네이밍 컨벤션)
