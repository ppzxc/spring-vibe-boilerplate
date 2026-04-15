---
description: 모듈별 금지 import, 컴포넌트별 필수 패턴, 검증 책임 분리, Tier 매핑
alwaysApply: true
---

# 검증 규칙

## 규칙 Tier 매핑

규칙 위반의 심각도와 강제 수단에 따라 3단계 Tier로 분류한다.

| Tier | 강제 수단 | 실패 시 대응 | 대상 규칙 |
|------|----------|-------------|----------|
| **Tier 1** | CI 자동 (ArchUnit, Gradle 의존성 검사) | 빌드 실패 — 즉시 수정 | D-1, D-2, D-3, A-1, A-4, AD-1, AD-2 |
| **Tier 2** | AI 자기검증 (import 스캔, 패턴 매칭) | AI가 자동 수정 후 재커밋 | D-4, D-6, D-8, D-13, D-14, A-2, A-3, A-6, A-7, A-9, A-10, AD-3, AD-5, AD-7, T-1~T-3 |
| **Tier 3** | 코드 리뷰 (사람) | 리뷰어 지적 시 수정 | D-5, D-9, D-11, D-12, A-5, A-8, AD-4, AD-6 |

- MUST: Tier 1 위반은 커밋 전에 반드시 해결한다.
- MUST: Tier 2 위반은 AI가 자기검증 체크리스트로 확인 후 자동 수정한다.
- SHOULD: Tier 3은 코드 리뷰에서 피드백을 받아 개선한다.

---

## 모듈별 금지 Import (Tier 1: CI 자동 검증)

### domain 모듈 — 위반 시 즉시 수정

MUST NOT: 아래 import가 domain 모듈에 존재하면 안 된다.

```
org.springframework.*
org.jooq.*
com.fasterxml.*
org.slf4j.*
org.apache.logging.*
jakarta.*
org.jspecify.*
java.time.Clock
```

주의: `Instant.now()` 직접 호출도 금지. Application이 `Instant`를 파라미터로 전달.

### application 모듈 — 위반 시 즉시 수정

MUST NOT:
```
org.springframework.*
org.jooq.*
com.fasterxml.*
org.jspecify.*
```

### adapter-input 모듈 — 위반 시 즉시 수정

MUST NOT (Domain 직접 참조 금지):
```
*.domain.model.*
*.domain.event.*
*.domain.service.*
*.domain.exception.*
```

## 컴포넌트별 필수 패턴 (Tier 2: AI 자기검증)

### Aggregate Root
- MUST: `private` 생성자. 외부에서 `new` 불가.
- MUST: `create()` static 팩토리 메서드 (신규 생성용) + `registerEvent()` 호출
- MUST: `reconstitute()` static 팩토리 메서드 (DB 복원용) — 이벤트 미발행
- MUST: 모든 파라미터에 `Objects.requireNonNull`
- MUST: `private final List<DomainEvent> domainEvents = new ArrayList<>()`
- MUST: `List<DomainEvent> pullDomainEvents()` — 반환 후 내부 비움
- MUST: UUIDv7 식별자 — `UUID.randomUUID()` 금지 (D-7, Part 1 §3)
- MUST: 비즈니스 로직 내부 캡슐화 — Getter/Setter만 있는 Anemic Model 금지 (D-5)
- MUST NOT: 다른 Aggregate를 객체로 참조 — ID 참조만 허용 (D-9)

### Value Object
- MUST: `record` 타입
- MUST: Compact Constructor에서 자기검증 (null 체크, 형식 검증)
- MUST NOT: setXxx, Builder 패턴 사용
- MUST: 도메인 의미 있는 모든 원시 값은 VO 래핑 — Primitive Obsession 금지 (D-7)

### Domain Event
- MUST: `{Subject}Event` sealed interface 안에 record로 선언 (D-13)
- MUST: 5필드 포함 — `UUID eventId`, `String eventType`, `UUID aggregateId`, `Instant occurredAt`, `long aggregateVersion`
- MUST: `eventId`, `aggregateId`는 `UUID` 직접 사용 (VO 래핑 아님)
- MUST: 필드 순서 — `occurredAt` → `aggregateVersion` 순서

### Domain Exception
- MUST: `{Subject}Exception` sealed class 안에 구현 클래스 선언 (D-13)
- MUST NOT: Spring/HTTP 의존성 포함 금지

### Domain Service
- MUST: 생성자 또는 메서드 파라미터에 `Objects.requireNonNull`
- MUST NOT: Port 호출 금지 — 필요한 데이터는 Application이 파라미터로 전달
  > 근거: ADR-0007

### Application Service (UseCase 구현체)
- MUST: 생성자 주입 (Spring 없이 테스트 가능하도록)
- MUST: 4가지 허용 역할만 수행: (1) 변환, (2) 조회, (3) 위임, (4) 저장
- MUST NOT: if/else 비즈니스 판단 — Domain으로 위임 (A-5)
- MUST NOT: `@Transactional` 어노테이션 (A-4)
  > 근거: ADR-0008
- MUST NOT: `ApplicationEventPublisher` 직접 주입/호출 (A-6)
- MUST NOT: `@Service`, `@Component` 어노테이션
- MUST NOT: 다른 UseCase 직접 호출 (A-8)
- MUST: 1 TX = 1 Aggregate — 하나의 UseCase에서 복수 Aggregate 변경 금지 (A-9)

### Input Port (UseCase 인터페이스)
- MUST: 모든 UseCase에 인터페이스 정의 (단순 CRUD도 예외 없음) (A-2)
  > 근거: ADR-0009
- MUST: 파라미터는 Command 또는 Query record
- MUST: 모든 조회도 UseCase 경유 — Controller→QueryPort 직접 호출(Bypass) 금지

### Output Port
- MUST: Load/Save/Query 3분할 (A-3)
  - `Load{Subject}Port` — 단건 조회, Optional 반환
  - `Save{Subject}Port` — 저장 (이벤트 수거 포함)
  - `{Subject}QueryPort` — 복합 조회
- MUST: 조회 메서드 반환 타입은 `Optional<T>` — null 반환 금지
- MUST NOT: Domain 모듈에 repository 인터페이스 금지 — Output Port는 Application에만 위치 (D-10)

### Command / Query / Result DTO
- MUST: `record` 타입 (A-10)
- MUST: Command/Query는 Compact Constructor에서 self-validation
- MUST NOT: Domain VO 타입을 필드로 사용 — 원시 타입 또는 `String`, `UUID` (A-7)

### SavePort 구현체 (PersistenceAdapter)
- MUST: `save(aggregate)` 호출 후 `aggregate.pullDomainEvents()`로 이벤트 수거
- MUST: 수거된 이벤트를 같은 TX 내에서 `ApplicationEventPublisher.publishEvent()` 발행 (AD-3)
- MUST: UPDATE 시 `WHERE version = ?` — affected rows == 0이면 `OptimisticLockException` (AD-7)
- MUST: DB 로드 시 `Aggregate.reconstitute()` 호출 (AD-5)
- MUST: 명시적 Mapper 사용 — Domain ↔ DB Record 직접 변환 금지 (AD-4)

### Controller
- MUST: Input Port(UseCase 인터페이스)에만 의존 (AD-1)
- MUST NOT: Domain 객체 직접 참조
- MUST NOT: 트랜잭션 시작 (T-2)
- MUST NOT: Adapter 간 직접 참조 (AD-2)
- MUST: JSpecify `@Nullable`/`@NonNull` 적용 (AD-6) — Adapter/Configuration에서만, Domain/Application 금지

### 트랜잭션
- MUST: UseCase가 TX 경계 — Configuration 프록시로 적용 (T-1, A-4)
- MUST NOT: Controller에서 TX 시작 (T-2)
- MUST NOT: Port 구현체 내부에서 독립 TX 시작 (T-3)

### Configuration 모듈
- MUST: Bean 와이어링, TX 프록시, EventTranslator, 공유 인프라 Bean만 수행한다.
- MUST NOT: Configuration 모듈에 if/else 비즈니스 판단, 도메인 로직, 데이터 변환 로직을 작성한다 (Part 3 §4.3).

### 추적성
- MUST NOT: Domain Event 페이로드에 `traceId` 포함 — OpenTelemetry가 메시지 헤더로 전파 (A-11)
- MUST NOT: Domain Event 페이로드에 `userId`(요청자), `tenantId` 포함 — 요청 컨텍스트는 Adapter가 메시지 헤더로 주입 (Part 12 §5.4)
- MUST: 비즈니스 컨텍스트(userId, tenantId, permissions) 전파에 `ScopedValue` (Java 25) 사용
- MUST NOT: `ThreadLocal` 사용 — Virtual Thread 환경 위험

## 검증 책임 분리

| 계층 | 검증 유형 | 예시 |
|------|-----------|------|
| Adapter (In) | 프로토콜 유효성 | JSON 파싱, 인증 토큰 형식 |
| Command / Query | 구조적 유효성 (Self-Validation) | null, 형식, 범위 |
| UseCase | 비즈니스 유효성 + VO 변환 | 이메일 중복, 권한 확인 |
| Domain VO | 값 유효성 (Self-Validation) | Email 형식, 이름 길이 |
| Domain Entity | 불변식 + Null-Safety | 잔액 >= 0, 상태 전이 |

MUST: 각 계층은 자신의 검증 범위만 책임지며, 하위 계층의 검증을 대행하지 않는다.

## PR 체크리스트

커밋 전 확인:

**Domain 순수성:**
- [ ] domain 모듈에 금지 import 없음 (D-1~D-4)
- [ ] application 모듈에 금지 import 없음 (A-1)
- [ ] Domain 객체에 public 생성자 없음 (D-8)
- [ ] VO가 record로 선언됨 (D-6)
- [ ] Domain Event에 5필드 포함됨 (D-13)
- [ ] Domain에 금지 접미사 없음 (D-12)
- [ ] 비즈니스 로직이 Entity/VO 내부에 있음 (D-5)
- [ ] 모든 VO가 `record`이며 원시 타입이 VO로 포장됨 (D-6, D-7)

**Port & Adapter:**
- [ ] Application Service에 @Transactional 없음 (A-4)
- [ ] 새 UseCase에 Input Port 인터페이스 존재 (A-2)
- [ ] Output Port가 null 반환하지 않고 Optional 반환 (A-3)
- [ ] Inbound Adapter가 Domain을 직접 참조하지 않음 (AD-1)
- [ ] Adapter 간 직접 참조 없음 (AD-2)
- [ ] 모든 계층 경계에서 명시적 DTO 변환 있음 (AD-4)

**이벤트 & 동시성:**
- [ ] SavePort 구현체가 pullDomainEvents() 호출함 (AD-3)
- [ ] Controller가 Domain 객체를 직접 참조하지 않음 (AD-1)
- [ ] SavePort UPDATE에 version 조건절 있음 (AD-7)
- [ ] 1 TX = 1 Aggregate 준수 (A-9)

**테스트:**
- [ ] Domain 테스트가 Spring 없이 순수 Java임

---

> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> 명시된 ADR 번호에 해당하는 `docs/decisions/` 파일을 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
