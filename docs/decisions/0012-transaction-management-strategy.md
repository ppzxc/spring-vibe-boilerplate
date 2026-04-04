---
status: accepted
date: 2026-04-02
decision-makers: ppzxc
---

# 트랜잭션 관리 전략: AutoConfiguration 데코레이터 패턴

## Context and Problem Statement

Hexagonal Architecture 원칙(ADR-0001)에 따라 application layer는 Spring에 의존할 수 없다.
그 결과 UseCase 구현체(`*Service`)에 `@Transactional`을 직접 붙일 수 없다.
멀티 Port UseCase(여러 outbound port를 한 UseCase에서 호출)의 원자성을 보장하는 트랜잭션 경계 관리 전략이 필요하다.

## Decision Drivers

* application layer 순수성 유지 (Spring 의존 금지, ArchUnit 강제)
* 멀티 Port UseCase에서 원자성 보장
* 트랜잭션 동작을 통합 테스트로 검증 가능해야 함
* 보일러플레이트 최소화

## Considered Options

* Option 1: TransactionPort (shared output port)
* Option 2: AutoConfiguration 데코레이터 (Bean 등록 시 `@Transactional` 프록시 적용)
* Option 3: Outbound Adapter 단위 `@Transactional`

## Decision Outcome

Chosen option: "Option 2: AutoConfiguration 데코레이터", because application layer 코드를 전혀 변경하지 않고 트랜잭션 경계를 적용할 수 있으며, 이미 UseCase Bean을 등록하는 역할을 하는 `boilerplate-application-autoconfiguration` 모듈과 책임이 일치한다.

### Consequences

* Good, because application layer가 Spring 의존 없이 순수 Java로 유지된다
* Good, because 멀티 Port UseCase에서 단일 트랜잭션으로 원자성이 보장된다
* Good, because 통합 테스트(`@SpringBootTest` + Testcontainers)에서 트랜잭션 롤백을 실제 DB로 검증할 수 있다
* Good, because Command/Query 분리에 따라 `readOnly` 최적화를 데코레이터에서 일괄 적용할 수 있다
* Bad, because UseCase가 추가될 때마다 autoconfiguration에 `@Bean` 등록이 필요하다 (기존 구조와 동일한 수준의 보일러플레이트)

### Confirmation

* `ApplicationArchitectureTest`가 통과해야 한다 — application layer에 Spring 의존이 없음을 보장
* 통합 테스트에서 UseCase가 `AopUtils.isAopProxy(useCase) == true`임을 확인할 수 있다
* 통합 테스트에서 UseCase 실행 중 예외 발생 시 모든 outbound port 변경이 롤백됨을 확인할 수 있다

## Pros and Cons of the Options

### Option 1: TransactionPort (shared output port)

`port.output.shared.TransactionPort` 인터페이스를 정의하고, UseCase 내부에서 명시적으로 `txPort.executeInTransaction(() -> { ... })`를 호출한다.

* Good, because 트랜잭션 경계가 코드에 명시적으로 드러난다
* Bad, because 트랜잭션 경계 설정이라는 기술적 관심사가 비즈니스 로직(UseCase)에 노출된다
* Bad, because 업계에서 논의는 많지만 실제 채택률이 낮다 — 대부분 과설계로 평가

### Option 2: AutoConfiguration 데코레이터

`boilerplate-application-autoconfiguration`에서 UseCase Bean 등록 시 `@Transactional`이 적용된 Spring AOP 프록시로 감싸 반환한다.

* Good, because application 코드 변경 없이 트랜잭션 적용 가능
* Good, because 이미 UseCase Bean 등록을 담당하는 모듈이므로 책임 일치
* Good, because Tom Hombergs의 *Get Your Hands Dirty on Clean Architecture*에서 엄격한 hexagonal 프로젝트의 정석 대안으로 권장
* Neutral, because UseCase 추가마다 `@Bean` 등록 필요 (기존 방식과 동일)

### Option 3: Outbound Adapter 단위 `@Transactional`

각 outbound adapter 메서드에 `@Transactional`을 붙인다.

* Good, because 별도 설정 없이 단순하다
* Bad, because 멀티 Port UseCase에서 원자성 보장 불가 — 두 번째 port 실패 시 첫 번째 port 변경은 이미 커밋됨
* Bad, because 단순 CRUD 외의 실제 비즈니스 시나리오에 적용 불가

## More Information

* 이 결정의 아키텍처 제약 근거: [ADR-0001](0001-hexagonal-architecture-and-cqrs.md)
* 가드레일 규칙: `.claude/rules/architecture.md` — "트랜잭션 관리 규칙 [ADR-0012]" 섹션
* Tom Hombergs, *Get Your Hands Dirty on Clean Architecture*, Manning 2nd ed. 2023 — Chapter 8: Organizing Code
* Alistair Cockburn, "Hexagonal Architecture" (2005) — 트랜잭션은 인프라 관심사로, application hexagon 외부에서 처리
