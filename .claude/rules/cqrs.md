---
description: CQRS Level 1 — Command/Query 분리, Output Port 3분할, Outbox 패턴, 이벤트 흐름
alwaysApply: true
---

# CQRS Rules

CQRS 규칙 — 항상 로드.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.

---

## 1. CQRS Level 정의

본 프로젝트는 **Level 1 CQRS**를 기본으로 적용한다.

| Level | 설명 |
|-------|------|
| **Level 1** | Command/Query 클래스 분리, 동일 DB (기본 적용) |
| Level 2 | 별도 Read Model/Store (확장 시 적용) |
| Level 3 | Event Sourcing (범위 외) |

Level 1 특성:
- Command Side와 Query Side가 동일 DB를 공유한다.
- Query Side는 **DB → DTO 직접 프로젝션 허용** — Domain Entity를 경유하지 않는다.
- Level 2 확장 시: QueryPort 구현체를 Cache, Read-Replica, 또는 별도 Projection Store로 교체. 구조는 그대로 유지.

> 근거: ADR-0004

---

## 2. Command/Query 분리 (A-7)

### 분리 원칙

- MUST: 외부 요청을 상태 변경(Command)과 상태 조회(Query)로 분리한다.
- MUST: Command와 Query를 처리하는 UseCase는 Input Port 인터페이스로 정의한다.

### A-7 Command/Query 필드 타입

- MUST: Command와 Query의 필드는 원시 타입(`String`, `Long`, `UUID` 등)으로 정의한다.
- MUST: Domain VO로의 변환은 UseCase(Application Service)가 수행한다.
- MUST NOT: Domain Value Object를 Command/Query 필드 타입으로 사용한다.

```java
// ❌ BAD — Command 필드에 Domain VO 사용
public record SuspendUserCommand(UserId userId, Instant now) {}  // Domain VO 금지

// ✅ GOOD — Command 필드는 원시 타입
public record SuspendUserCommand(String userId, String reason) {
    public SuspendUserCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }
}

// ✅ GOOD — Query 필드도 원시 타입
public record FindUserByIdQuery(String userId) {
    public FindUserByIdQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}
```

---

## 3. Command Self-Validation

Command 객체는 **생성 시점에 항상 유효한 상태**를 보장한다. 유효하지 않은 Command가 UseCase에 도달하는 것을 원천 차단한다.

- MUST: Command는 Compact Constructor에서 필수 필드 검증을 수행하고, 실패 시 즉시 예외를 던진다.
- MUST NOT: UseCase 내부에서 Command 필드를 일일이 null 체크한다.

검증 책임 분리:

| 계층 | 검증 유형 | 예시 |
|------|-----------|------|
| Adapter (In) | 프로토콜 유효성 | JSON 파싱, 인증 토큰 |
| **Command / Query** | **구조적 유효성 (Self-Validation)** | null, 형식, 범위 |
| **UseCase** | **비즈니스 유효성 + VO 변환** | 이메일 중복, 권한 |
| **Domain VO** | **값 유효성 (Self-Validation)** | Email 형식, 이름 길이 |
| **Domain Entity** | **불변식 + Null-Safety** | 잔액 ≥ 0, 상태 전이 |

```
[Command 생성 흐름]

Adapter(In): Request → Command 변환 (원시 타입 → 원시 타입, Self-Validation)
    ↓  유효한 Command만 통과
UseCase: Command → Domain VO 변환 (원시 타입 → record VO)
    ↓  VO Compact Constructor가 도메인 수준 값 검증
Domain: Entity 행위 호출
```

---

## 4. Command 하위 호환성

Core에 정의된 Command와 Query는 **하위 호환성(Backward Compatibility)**을 유지해야 한다.

| 허용 | 금지 |
|------|------|
| 필드 **추가** (Optional, 기본값 포함) | 기존 필드 **제거** |
| 새로운 Command/Query 타입 신설 | 기존 필드의 **타입 변경** |
| — | 기존 필드의 **의미(Semantics) 변경** |
| — | Core Command/Query에 `V1`, `V2` 접미사 버저닝 |

- MUST: Command/Query는 하위 호환성을 유지한다.
- MUST: 외부 API 스펙 변경은 Adapter(In)에서 흡수한다.
- MUST NOT: 기존 필드를 제거, 타입 변경, 의미 변경한다.
- MUST NOT: Core의 Command/Query에 `V1`, `V2` 접미사 버저닝을 사용한다.

---

## 5. Command 멱등성

상태를 변경하는 Command는 멱등성(Idempotency)을 고려해야 한다.

- MUST: 상태를 변경하는 Command는 멱등성을 고려한다.
- SHOULD: 생성형 Command(결제, 주문 등)에는 Idempotency Key를 적용한다.

| 전략 | 동작 방식 | 적합한 상황 |
|------|-----------|-------------|
| **자연 멱등** | 동일 입력 → 동일 결과 (UPSERT 등) | 상태 덮어쓰기형 Command |
| **Idempotency Key** | 클라이언트가 고유 키를 전송, DB Unique Constraint로 중복 차단 | 생성형 Command (결제, 주문 등) |
| **조건부 실행** | 현재 상태(버전, 상태값)를 확인 후 실행 | 상태 전이형 Command (Optimistic Lock) |

---

## 6. Output Port 3분할 (A-3)

Output Port는 Load / Save / Query 3개로 분리한다. 하나의 인터페이스가 두 역할을 겸하는 것을 금지.

| Port | 역할 | Side | 반환 타입 |
|------|------|------|-----------|
| `Load{Subject}Port` | Domain 객체 로드 | Command | Domain Entity. **`Optional` 반환 강제** |
| `Save{Subject}Port` | Aggregate 저장 + 이벤트 수거 | Command | void / Entity |
| `{Subject}QueryPort` | DTO 직접 반환. Aggregate 비경유 | Query | DTO |

- MUST: Command Port와 Query Port를 분리한다.
- MUST: Command Side는 `Load{Subject}Port` + `Save{Subject}Port`로 분리한다. Domain Entity를 반환.
- MUST: `Load{Subject}Port`의 find 메서드는 `Optional`을 반환한다.
- MUST: Query Side는 `{Subject}QueryPort`로 정의한다. DTO를 직접 반환. Aggregate를 경유하지 않는다.
- MUST: SavePort 구현체가 `pullDomainEvents()`로 이벤트를 수거하여 Outbox에 저장한다.

> 근거: ADR-0004

```java
// Load Port — Command Side, Optional 반환 강제
public interface LoadUserPort {
    Optional<User> findById(UserId id);
    boolean existsByName(UserName name);
}

// Save Port — Command Side, 이벤트 수거는 구현체(Adapter)가 수행
public interface SaveUserPort {
    void save(User user);
}

// Query Port — Query Side, DTO 직접 반환
public interface UserQueryPort {
    Optional<UserSummary> findSummaryById(String userId);
    List<UserSummary> findAll();
}
```

Command Side 흐름:

```java
// ✅ GOOD — Application Service에서 원시 타입 → VO 변환 후 LoadPort 사용
var userId = new UserId(UUID.fromString(command.userId()));
var user = loadPort.findById(userId).orElseThrow();
user.suspend(command.reason(), clock.instant());
savePort.save(user);  // Adapter 내부에서 pullDomainEvents() → Outbox
```

Query Side 흐름:

```java
// Query Side — Domain Entity를 거치지 않음 (직접 DTO 반환)
public class FindUserByIdService implements FindUserByIdUseCase {
    private final UserQueryPort queryPort;

    @Override
    public UserSummary execute(FindUserByIdQuery query) {
        return queryPort.findSummaryById(query.userId())
            .orElseThrow(() -> new UserNotFoundException(query.userId()));
    }
}
```

---

## 7. 조회 UseCase Bypass 금지

- MUST: 모든 조회는 UseCase(Input Port)를 경유한다.
- MUST: Query UseCase가 단순 위임(`return queryPort.findAll()`)만 수행하더라도 Input Port 정의를 생략하지 않는다.
- MUST NOT: Controller에서 QueryPort(Output Port)를 직접 호출한다 (Bypass 금지).

```java
// ❌ BAD — Controller가 QueryPort를 직접 호출 (Bypass)
@RestController
public class UserController {
    private final UserQueryPort queryPort;  // Output Port 직접 주입
    @GetMapping("/users")
    public List<UserSummary> list() {
        return queryPort.findAll();          // UseCase 없음 — 금지
    }
}

// ✅ GOOD — UseCase 경유
@RestController
public class UserController {
    private final ListUsersUseCase useCase;  // Input Port
    @GetMapping("/users")
    public List<UserSummary> list() {
        return useCase.execute(new ListUsersQuery());
    }
}
```

---

## 8. 1 TX = 1 Aggregate (A-9)

- MUST: 하나의 UseCase에서 복수 Aggregate를 동일 트랜잭션으로 변경하는 것을 금지한다.
- MUST: 복수 Aggregate 변경이 필요하면 이벤트 기반 분리를 사용한다.

> 근거: ADR-0004

```
[1st TX] Aggregate A 변경 → Domain Event 발행 → Outbox 저장 (같은 TX)
[2nd TX] adapter-input-event: Event 수신 → Command 생성 → UseCase 호출 → Aggregate B 변경
```

- 수신 모듈: Event 핸들러는 `adapter-input-event` 모듈에 위치한다. 같은 BC 내 Domain Event든, 다른 BC의 Integration Event든 동일 패턴.
- 수신 패턴: **Event → Command(원시 타입) → UseCase 호출 → 별도 TX**

```java
// ❌ BAD — 두 Aggregate를 한 TX에서 변경
public void execute(TransferCommand cmd) {
    var source = loadAccountPort.findById(new AccountId(cmd.sourceId())).orElseThrow();
    var target = loadAccountPort.findById(new AccountId(cmd.targetId())).orElseThrow();
    source.withdraw(cmd.amount());
    target.deposit(cmd.amount());          // 같은 TX에서 두 Aggregate 변경 — 금지
    saveAccountPort.save(source);
    saveAccountPort.save(target);
}
```

---

## 8.1. 동시성 제어 원칙

멱등성이 "동일 요청의 중복 실행"을 방어한다면, 동시성 제어는 "서로 다른 요청이 동일 리소스를 동시에 변경하려 할 때"의 경합을 방어한다.

| 전략 | 동작 방식 | 적합한 상황 |
|------|-----------|-------------|
| **Optimistic Lock** | Entity의 version 필드 기반, 갱신 시 version 불일치면 실패 | 충돌 빈도가 낮은 일반적인 경우 (기본 권장) |
| **Pessimistic Lock** | DB 레코드를 `SELECT ... FOR UPDATE`로 잠금 | 충돌 빈도가 높고 반드시 순차 처리가 필요한 경우 |
| **Compare-And-Set** | 상태 전이 조건을 WHERE 절에 포함하여 원자적 갱신 | 상태 머신 기반 전이 (예: `PENDING → APPROVED`) |

- MUST: 모든 SavePort 구현체는 UPDATE 시 Aggregate의 `version` 필드를 WHERE 조건에 포함한다.
- MUST: 영향 행이 0이면 도메인 예외를 발생시킨다 (Optimistic Lock 실패).
- MUST: Pessimistic Lock 사용 시 근거를 ADR에 명시한다.
- SHOULD: Optimistic Lock을 기본으로 선택한다.

---

## 9. 이벤트 흐름 (A-6)

### A-6 Application Service에서 EventPublisher 직접 호출 금지

- MUST NOT: Application Service에서 EventPublisher를 직접 호출한다.
- MUST: Application Service는 `savePort.save(aggregate)`만 호출한다.
- MUST: 이벤트 수거 및 Outbox 저장은 SavePort 구현체(Adapter)가 수행한다.

이벤트 흐름:

```
Request → Command → UseCase → Domain Entity
                       ↓              ↓
                   SavePort      Domain Event (내부 목록에 등록)
                       ↓
                  Adapter 내부:
                    ├─ Entity → DB 저장
                    └─ pullDomainEvents() → Outbox 저장 (같은 TX)
                  COMMIT
                       ↓ (별도 프로세스)
                  Outbox Poller / CDC
                       ↓
                  EventPublisher Adapter
                  ┌──────────┼──────────┐
             In-Process   Webhook     Message Queue
```

수신 패턴:

```java
// adapter-input-event 모듈: Event → Command → UseCase
@ApplicationModuleListener
public class UserEventConsumer {
    private final SomeUseCase someUseCase;

    public void on(UserRegisteredEvent event) {
        // Event → Command(원시 타입) 변환
        var command = new SomeCommand(event.aggregateId().toString());
        // UseCase 호출 → 별도 TX
        someUseCase.execute(command);
    }
}
```

---

## 10. Outbox 패턴

### Spring Modulith EventPublicationRegistry

본 프로젝트는 Spring Modulith의 **Event Publication Registry**를 사용한다.

- `event_publication` 테이블에 이벤트와 DB 변경을 같은 TX로 원자적 저장.
- 컨슈머 처리 완료 시 `completed_at` 갱신.
- 미완료 이벤트는 자동 재발행 (at-least-once 보장).

```
[Outbox 흐름]

UseCase (TX Proxy에서 TX START)
    └─ SavePort.save(aggregate)
         ↓
       Adapter 내부:
         ├─ Entity → DB 저장 (jOOQ)
         └─ aggregate.pullDomainEvents()
              → ApplicationEventPublisher.publishEvent(event)
              → Spring Modulith: event_publication 테이블에 자동 저장
COMMIT
    ↓ (별도 스레드/프로세스)
Spring Modulith EventPublicationRegistry
    ↓
@ApplicationModuleListener (adapter-input-event)
    ↓
이벤트 처리 완료 → completed_at 갱신
```

원칙:
- MUST: 상태 변경(Entity)과 이벤트 저장(Outbox)은 동일 트랜잭션으로 원자성을 보장한다.
- MUST: 이벤트 컨슈머는 at-least-once 수신을 전제하고 중복 수신을 방어한다.
- MUST: EventPublisher Output Port는 코어에 정의한다. 단, Application Service에서 직접 호출하지 않는다.

### Outbox 패턴 비교

| 패턴 | 메커니즘 | 장점 | 단점 |
|------|---------|------|------|
| **Spring Modulith** | event_publication 테이블 자동 | 설정 최소, 자동 재발행 | 단일 JVM |
| **Polling** | SELECT FOR UPDATE SKIP LOCKED | 단순 | 지연, DB 부하 |
| **CDC (Debezium)** | Kafka Connect | 실시간, DB 부하 없음 | 인프라 복잡 |

---

## 전체 CQRS 흐름 다이어그램

| 구분 | Command Side | Query Side |
|------|-------------|------------|
| 진입점 | POST, PUT, PATCH, DELETE | GET |
| Input | Command (self-validated, 원시 타입 필드) | Query (self-validated, 원시 타입 필드) |
| Input Port | UseCase 인터페이스 (Command) | UseCase 인터페이스 (Query) |
| 트랜잭션 | Configuration Proxy에서 R/W TX | Configuration Proxy에서 R/O TX |
| Application | UseCase Service (순수 Java, VO 변환 + 오케스트레이션) | UseCase Service (순수 Java) |
| Domain | Entity + VO (Rich Domain Model) | 미경유 |
| Output Port | LoadPort + SavePort | QueryPort |
| Event | SavePort 내부에서 Outbox 저장 (원자성 보장, at-least-once) | — |
| Aggregate 제약 | 1 TX = 1 Aggregate | — |

---

## 11. 이벤트 진화 (Backward Compatibility)

Domain Event와 Integration Event는 컨슈머가 존재하는 한 **하위 호환성**을 유지해야 한다.

| 변경 유형 | 허용 여부 | 방법 |
|---------|---------|------|
| 페이로드 필드 **추가** (nullable 또는 기본값) | 허용 | 기존 컨슈머는 새 필드를 무시 |
| 5-field 표준 필드 **제거** | **금지** | — |
| 기존 필드 **타입 변경** | **금지** | 새 이벤트 타입 신설 |
| 기존 필드 **의미 변경** | **금지** | 새 이벤트 타입 신설 |
| 새 이벤트 타입 **신설** | 허용 | sealed interface permits 갱신 |

- MUST: 이벤트 스키마 변경 시 하위 호환성을 검토한다.
- MUST: 하위 호환이 불가능한 변경은 **새 이벤트 타입**을 신설하고, 구 버전은 일정 기간 병행 발행한다.
- MUST NOT: 기존 이벤트의 필드 타입이나 의미를 변경한다.
- MUST NOT: 이벤트 타입에 `V1`, `V2` 버전 접미사를 붙인다. 대신 개념이 다른 새 타입으로 분리한다.

**이벤트 진화 예시**

```java
// ✅ GOOD — 필드 추가 (하위 호환)
// v1
public record UserRegisteredEvent(
    UUID eventId, String eventType, UUID aggregateId,
    Instant occurredAt, long aggregateVersion,
    String userName
) implements UserEvent {}

// v2 — email 추가 (새 컨슈머만 사용, 기존 컨슈머 영향 없음)
public record UserRegisteredEvent(
    UUID eventId, String eventType, UUID aggregateId,
    Instant occurredAt, long aggregateVersion,
    String userName,
    String email   // 추가 — 기존 컨슈머는 무시 가능
) implements UserEvent {}

// ❌ BAD — 타입 변경 (하위 비호환)
// userName: String → UserName(VO) 변경 금지
```

---

## 12. CloudEvents 매핑 (참조)

Spring Modulith의 Integration Event는 [CloudEvents v1.0](https://cloudevents.io/) 표준과 매핑된다.

| CloudEvents 속성 | Spring Modulith / 본 프로젝트 대응 |
|-----------------|----------------------------------|
| `id` | `eventId` (UUIDv7) |
| `source` | 서비스명 + BC명 (`boilerplate/identity`) |
| `type` | `eventType` (예: `UserRegisteredEvent`) |
| `time` | `occurredAt` (ISO-8601) |
| `datacontenttype` | `application/json` |
| `data` | 이벤트 페이로드 (나머지 필드) |
| `traceparent` | W3C Trace Context 헤더 (OTel 자동 주입) |

- MUST: Integration Event를 외부 메시지 브로커(Kafka, RabbitMQ 등)로 확장 시 CloudEvents 표준을 따른다.
- MUST NOT: `traceparent`를 이벤트 페이로드(`data`)에 포함한다. 메시지 헤더로 전파 (observability.md §7 참조).
- SHOULD: `source` 필드는 `{서비스명}/{bc명}` 형식으로 설정한다.

---

## fallback 지시문

---
> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> 명시된 ADR 번호에 해당하는 `docs/decisions/` 파일을 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
