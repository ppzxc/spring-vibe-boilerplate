# Pragmatic DDD & Hexagonal Architecture Constitution (v8.0)

**"비즈니스의 의도는 도메인(DDD)으로 모델링하고, 기술의 오염은 헥사고날(Hexagonal)로 방어하되, 생산성을 위해 실용주의(Pragmatism)와 타협하고, 운영 안정성(Operations)으로 완성한다."**

## 1. 개요 (Introduction)
본 문서는 복잡한 비즈니스 요구사항을 소프트웨어로 구현할 때 우리 팀이 반드시 지켜야 할 **아키텍처 표준 및 통제 기준**입니다.
도메인의 순수성을 방어하고, 100% jOOQ 기반의 영속성을 관리하되, **과도한 인터페이스 분리(오버엔지니어링)로 인한 개발 피로도를 막기 위해 '실용적 DDD' 패턴과 '멀티 모듈 격리'를 채택**합니다. 모든 Pull Request는 이 헌법을 통과해야 합니다.

---

## 2. 전략적 설계와 경계 관리 (Strategic Design)
바운디드 컨텍스트(Bounded Context)는 단순한 패키지 분리가 아닌 **완벽한 독립 시스템**으로 간주해야 합니다.

* **직접 참조 금지:** 다른 Context의 Domain 객체(Entity, VO, DB Record)를 절대 `import` 하지 않습니다.
* **데이터 전달:** Context 간 데이터 전달은 오직 **DTO(동기 API)** 또는 **도메인 이벤트(비동기 메시징)**로만 수행합니다.

---

## 3. 패키지 및 멀티 모듈 구조 표준 (Structure Standard)
`[Hexagonal Architecture 규칙]`

자바 예약어 충돌(`interface`)을 피하고, 헥사고날 순수주의가 유발하는 '보일러플레이트 지옥'을 막기 위해 **국내외 빅테크의 실무 표준 4계층 아키텍처**를 채택합니다. 시스템의 규모에 따라 단일 모듈 또는 멀티 모듈 방식을 선택하되, 의존성의 방향은 항상 바깥 계층에서 핵심 도메인을 향해야 합니다.

### 3.1 단일 모듈 패키지 표준 (Single-Module Layout)
초기 구축 시점이나 도메인 복잡도가 낮을 때 사용하는 디렉토리 구조입니다.
```text
io.github.ppzxc.boilerplate.{bounded-context}/
  ├── domain/                # [Hexagon Core] 핵심 비즈니스 로직 (의존성 0%)
  │   ├── model/             # Entity, Value Object, Aggregate Root
  │   ├── repository/        # Repository 인터페이스 (DIP)
  │   └── event/             # Domain Events
  │
  ├── application/           # 흐름 제어 및 트랜잭션 관리
  │   └── service/           # Use Case 구현체
  │
  ├── presentation/          # [Inbound Adapters] 외부 노출 / 통신
  │   ├── api/               # REST Controllers
  │   └── dto/               # Request/Response DTOs
  │
  └── infrastructure/        # [Outbound Adapters] 기술적 구현체
      ├── persistence/       # jOOQ Repository 구현체, Outbox Entity
      └── messaging/         # 메시지 브로커 Producers & Consumers (선택)
```

### 3.2 멀티 모듈 표준 (Multi-Module Layout) - 실무 권장
컴파일 타임에 아키텍처 위반을 원천 차단하기 위해 **물리적인 빌드 모듈(Gradle/Maven)**로 계층을 분리합니다.

```text
boilerplate/                                        # Root Project (Gradle Kotlin DSL)
├── build.gradle.kts                              # 공통 플러그인, 버전 카탈로그
├── settings.gradle.kts                           # 모듈 include
├── gradle/
│   └── libs.versions.toml                        # Version Catalog
│
│ ── [Hexagon Core] boilerplate-domain ──────────────────
│    순수 비즈니스 모듈 — 외부 의존성 0%
│    허용: 순수 Java/Kotlin, jakarta.validation (선택)
│    금지: Spring, jOOQ, Jackson, Lombok
│
├── boilerplate-domain/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       ├── domain/
│       │   ├── model/                            # Aggregate Root, Entity, Value Object
│       │   │   ├── Tenant.java                   # Aggregate Root (POJO, 불변 설계)
│       │   │   ├── TenantId.java                 # VO — 식별자 (UUID 래퍼)
│       │   │   ├── TenantStatus.java             # VO — enum
│       │   │   └── OwnerId.java                  # VO — 다른 Aggregate의 ID 참조
│       │   │
│       │   ├── service/                          # Domain Service
│       │   │   └── TenantDomainService.java      # 여러 Aggregate 간 순수 비즈니스 조율
│       │   │
│       │   ├── repository/                       # Output Port (인터페이스만)
│       │   │   └── TenantRepository.java         # 구현체는 boilerplate-infrastructure-persistence-jooq에 위치
│       │   │
│       │   ├── event/                            # Domain Event
│       │   │   ├── TenantCreatedEvent.java       # 불변 record, Aggregate ID + 타임스탬프
│       │   │   └── TenantSuspendedEvent.java
│       │   │
│       │   └── exception/                        # Domain Exception
│       │       ├── TenantNotFoundException.java   # 비즈니스 예외만 (HTTP 의존 금지)
│       │       └── TenantAlreadyExistsException.java
│       │
│       └── application/
│           ├── service/                          # Application Service (오케스트레이터)
│           │   ├── TenantCommandService.java     # Command: 조회 -> 위임 -> 저장/발행
│           │   └── TenantQueryService.java       # Query: CQRS 조회 Port 호출
│           │
│           ├── port/
│           │   ├── input/                        # Input Port (선택적 사용)
│           │   │   └── CreateTenantUseCase.java  # 필요할 때만 정의 (Pragmatic)
│           │   └── output/                       # Output Port
│           │       ├── TenantEventPublisher.java  # 이벤트 발행 추상화
│           │       ├── TenantQueryPort.java       # CQRS 조회 전용 Port
│           │       └── ExternalIdpClient.java     # 외부 시스템 연동 추상화
│           │
│           └── dto/                              # Application DTO (계층 간 전달)
│               ├── TenantInfo.java               # Query 결과 DTO (fetchInto 대상)
│               └── TenantSummary.java
│
│ ── [Inbound Adapters] boilerplate-application-api ─────────
│    Spring Boot 실행 모듈 — Controller, 글로벌 설정
│    의존성: boilerplate-domain, boilerplate-infrastructure-*
│
├── boilerplate-application-api/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       ├── presentation/
│       │   ├── api/                              # REST Controller
│       │   │   ├── TenantController.java         # @RestController, 요청 위임만
│       │   │   └── TenantAdminController.java    # 백오피스용 (CRUD 예외 정책 대상)
│       │   │
│       │   ├── dto/
│       │   │   ├── request/                      # Request DTO (@Valid 바인딩)
│       │   │   │   ├── CreateTenantRequest.java
│       │   │   │   └── UpdateTenantRequest.java
│       │   │   └── response/                     # Response DTO
│       │   │       ├── TenantResponse.java
│       │   │       └── TenantListResponse.java
│       │   │
│       │   └── advice/                           # 글로벌 예외 핸들러
│       │       └── GlobalExceptionHandler.java   # @ControllerAdvice -> ERR-IAM-001
│       │
│       └── config/                               # Spring 설정
│           ├── WebConfig.java
│           ├── SecurityConfig.java
│           └── CorsConfig.java
│
│ ── [Outbound Adapters] boilerplate-infrastructure-persistence-jooq ────
│    jOOQ 영속성 구현 — Repository 구현체, Outbox
│    의존성: boilerplate-domain, jOOQ, Flyway
│    금지: Spring Web
│
├── boilerplate-infrastructure-persistence-jooq/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/io/github/ppzxc/boilerplate/
│       │   └── infrastructure/
│       │       └── persistence/
│       │           ├── repository/               # Repository 구현체
│       │           │   └── TenantRepositoryImpl.java
│       │           │       # implements TenantRepository (domain port)
│       │           │       # 수동 매핑: POJO <-> jOOQ Record
│       │           │       # 낙관적 락: recordVersionFields 활용
│       │           │
│       │           ├── query/                    # CQRS Query 구현체
│       │           │   └── TenantQueryAdapter.java
│       │           │       # implements TenantQueryPort
│       │           │       # jOOQ Type-Safe SQL -> .fetchInto(DTO.class)
│       │           │
│       │           ├── mapper/                   # 수동 매핑 유틸
│       │           │   └── TenantRecordMapper.java
│       │           │       # Aggregate <-> jOOQ Record 변환
│       │           │
│       │           ├── outbox/                   # Outbox Pattern
│       │           │   ├── OutboxEntry.java      # outbox 테이블 매핑
│       │           │   ├── OutboxRepository.java
│       │           │   └── OutboxRelayScheduler.java
│       │           │       # 주기적 폴링 -> 브로커 전송, At-Least-Once
│       │           │
│       │           └── config/                   # jOOQ/Flyway 설정
│       │               ├── JooqConfig.java       # DSLContext, recordVersionFields
│       │               └── FlywayConfig.java
│       │
│       └── main/resources/
│           └── db/migration/                     # Flyway 마이그레이션
│               ├── V001__create_tenant.sql
│               ├── V002__create_outbox.sql
│               └── V003__add_version_column.sql
│
│ ── [Support] boilerplate-common ───────────────────
│    공통 유틸리티 — 에러 코드, Trace ID, 공통 응답 포맷
│    의존성: 순수 Java 또는 SLF4J만
│
└── boilerplate-common/
    ├── build.gradle.kts
    └── src/main/java/io/github/ppzxc/boilerplate/
        └── common/
            ├── error/                            # 표준 에러 코드
            │   ├── ErrorCode.java                # enum: ERR-IAM-001 등
            │   └── ErrorResponse.java            # RFC 9457 Problem Details 호환
            │
            ├── trace/                            # Correlation ID
            │   └── TraceContext.java              # ThreadLocal 기반 Trace ID 전파
            │
            └── page/                             # 페이지네이션
                └── CursorPageResult.java         # 커서 기반 + RFC 8288 Link 호환
```

* **[CRITICAL] 인프라 간 순환 참조 금지:** `boilerplate-infrastructure-*` 모듈 간의 직접 참조는 엄격히 금지됩니다.

### 3.3 선택적 확장 모듈 (Optional Modules)
메시지 브로커가 결정되면 `boilerplate-infrastructure-messaging-{broker}` 모듈을 추가합니다. 모듈명 예시: `boilerplate-infrastructure-messaging-kafka`, `boilerplate-infrastructure-messaging-rabbitmq`. 의존성은 `boilerplate-domain`만 허용하며, 다른 `boilerplate-infrastructure-*` 모듈 직접 참조는 금지합니다. `TenantEventPublisher`(Output Port)의 구현체를 이 모듈에 배치합니다.

### 3.4 실용적 매핑(Mapping) 허용 정책
* `presentation` 계층(또는 `boilerplate-application-api` 모듈)의 DTO를 `application` 계층의 메서드 파라미터로 바로 전달하는 것을 허용합니다. (불필요한 Inbound Port/DTO 변환 생략)
* **단, DTO가 `domain` 내부 로직으로 침투하는 것은 절대 금지합니다.**

---

## 4. 계층형 아키텍처 통제 (Layered Control)

### 4.1 Application Layer (응용 계층)
Application Service는 오케스트레이터(지휘자) 역할만 수행합니다.

* **[FORBIDDEN] 금지 사항 (Anti-Patterns):**
    * 비즈니스 정책 판단 (if/else 로 비즈니스 룰 분기)
    * 반복문 내부에서의 복잡한 도메인 계산
* **[ALLOWED] 허용되는 단 3가지 역할:**
    1. **조회:** Repository를 통한 Aggregate 로드
    2. **위임:** 도메인 객체의 행위(메서드) 호출
    3. **저장/발행:** 상태 변경 완료 후 DB 저장 및 이벤트 발행

### 4.2 외부 연동 (External Integration)
`[Hexagonal Architecture 규칙]`
* **어댑터 격리:** 외부 API 호출, AWS S3 업로드 등 외부 기술 연동은 모두 `infrastructure` 계층에서만 수행합니다.
* **의존성 역전(DIP):** Application Layer는 `WebClient`나 `RestTemplate` 같은 기술 키워드를 전혀 몰라야 하며, 도메인 언어로 작성된 인터페이스(Port)에만 의존합니다.

---

## 5. 전술적 설계와 Aggregate (Tactical Design)

* **크기 최소화:** Aggregate는 가능한 작게 유지하여 락(Lock) 경합을 줄입니다.
* **ID 참조:** 다른 Aggregate를 참조할 때는 객체 참조가 아닌 **ID 참조**를 사용합니다. (BAD: `tenant.getOwner().getName()` -> GOOD: `tenant.getOwnerId()`)
* **식별자 생성 (UUID/ULID):** 모든 Aggregate Root는 DB에 의존(Auto Increment)하지 않고, **객체 생성(new) 즉시 UUID/ULID 식별자**를 가져야 합니다.
* **트랜잭션 경계:** **1 Transaction = 1 Aggregate.** 여러 Aggregate의 변경이 필요하다면 반드시 도메인 이벤트를 발행하여 비동기로 처리합니다.

---

## 6. 100% jOOQ 영속성 및 CQRS 전략
JPA의 마법을 배제하고 jOOQ를 사용하여 데이터베이스 통제권을 완벽하게 확보합니다.

### 6.1 Command (상태 변경 로직)
`[Hexagonal Architecture 규칙]`
* **도메인 순수성:** `domain` 내부에 jOOQ의 `Table`, `UpdatableRecord` 관련 코드가 절대 침투해서는 안 됩니다.
* **수동 매핑:** `infrastructure`에 위치한 `RepositoryImpl`은 순수 POJO Aggregate를 인자로 받아, 내부에서 jOOQ `Record`로 수동 변환(Mapper)하여 `store/insert`를 수행합니다.

### 6.2 Query (조회 로직)
* **도메인 우회 (CQRS 실용주의):** 화면 조회를 위해 Repository나 Domain Entity를 거치지 않습니다.
* **직접 프로젝션:** `QueryService`에서 jOOQ의 Type-Safe SQL을 작성하고, `.fetchInto(DTO.class)`를 사용해 `presentation` 계층의 DTO로 데이터를 즉시 꽂아 넣습니다.

### 6.3 단순 CRUD 예외 정책
`[Pragmatic Exception]`
* 아래 조건을 **모두 만족하는** 경우에 한해, **`boilerplate-domain`을 거치지 않고 `boilerplate-application-api`에서 `boilerplate-infrastructure-persistence-jooq`를 직접 호출하여 jOOQ Record를 조작하는 것을 제한적으로 허용**합니다.
    * Read-only 조회/통계 화면 (목록, 대시보드, 리포트)
    * 도메인 이벤트 발행이 불필요한 마스터 코드 관리 (코드 테이블, 설정값 CRUD)
* **핵심 Aggregate의 상태 변경은 백오피스/어드민이라 하더라도 반드시 도메인을 거쳐야 합니다.** "단순 CRUD"로 시작했더라도 비즈니스 룰(알림, 권한 체크, 이벤트 발행)이 추가되는 시점에 즉시 도메인 계층으로 이전합니다.

### 6.4 동시성 제어 (Concurrency Control)
* Aggregate Root 업데이트 시 낙관적 락(Optimistic Lock)을 강제합니다. (jOOQ `recordVersionFields` 설정 활용)

---

## 7. 도메인 이벤트와 신뢰성 (Event Reliability)
`[Operational Safeguard]` 분산 환경에서 데이터 정합성을 지키는 핵심 생명선입니다.

* **발행 보장 (Outbox Pattern, At-Least-Once):** 비즈니스 상태 변경과 이벤트 기록을 동일한 로컬 DB 트랜잭션으로 묶어 저장한 후, 별도의 릴레이 워커가 메시지 브로커로 전송하여 유실을 막습니다. 다중 인스턴스 환경에서는 `SELECT ... FOR UPDATE SKIP LOCKED`로 폴링 경합을 방지합니다.
* **순서 보장 (Event Ordering):** 동일 Aggregate에서 발생한 이벤트는 순서가 보장되어야 합니다. 브로커의 순서 보장 메커니즘을 활용하여 **Aggregate ID 기반 파티셔닝**을 적용하십시오. (예: Kafka Partition Key, RabbitMQ Consistent Hash Exchange)
* **멱등성 (Idempotency):** 네트워크 재시도 등으로 인해 컨슈머가 동일한 이벤트를 2번 이상 수신해도 데이터가 꼬이지 않도록 로직을 멱등하게 설계합니다.
* **장애 복구 (Retry & DLQ):** 컨슈머 실패 시 점진적 재시도(Exponential Backoff)를 수행하며, 최종 실패한 메시지는 반드시 **DLQ(Dead Letter Queue)**로 격리하고 모니터링 알람을 발생시켜야 합니다.

---

## 8. 운영 관측성 및 스키마 관리 (Observability & Schema)
`[Operational Safeguard]`

* **추적성 (Tracing):** 모든 외부 API 요청, 내부 도메인 처리, 발행된 이벤트에는 동일한 **Correlation ID(Trace ID)**가 포함되어야 합니다.
* **스키마 관리:** 모든 DB 변경은 `Flyway` 또는 `Liquibase`로만 관리합니다. jOOQ Codegen은 반드시 마이그레이션이 끝난 스키마를 기반으로 실행되어야 합니다.

---

## 9. 예외 처리 원칙 (Exception Policy)
`[Hexagonal Architecture 규칙]`
* **Domain 예외:** `domain`은 오직 비즈니스 예외만 발생시킵니다. (`@ResponseStatus` 등 HTTP 기술 의존성 금지)
* **Infra 예외 변환 (Translation):** DB 예외(jOOQ `DataAccessException`)나 인프라 연동 예외는 반드시 `Application` 또는 `Infrastructure` 계층에서 Catch하여 의미 있는 도메인 예외로 변환해야 합니다.
* **표준 에러 규격:** 외부로 나가는 모든 예외는 `@ControllerAdvice`를 통해 팀 표준 규격(예: `ERR-IAM-001`)으로 변환됩니다.

---

## 10. 테스트 품질 및 강제 (Test & Enforcement)

### 10.1 테스트 전략 비율
* **[70%] Domain Test:** 외부 프레임워크(Spring, DB)가 없는 순수 자바 단위 테스트.
* **[15%] Application Test:** Mocking을 활용하여 Repository 조회, 판단 위임, 저장/이벤트 흐름이 정상적인지 검증.
* **[10%] Infrastructure Integration Test:** Testcontainers로 실제 DB를 띄워 jOOQ SQL 매핑, 낙관적 락 충돌, Outbox 저장/폴링이 정상 동작하는지 검증. jOOQ standalone 아키텍처에서 실제 버그가 가장 많이 발생하는 계층.
* **[5%] E2E Test:** 실제 통합 환경의 핵심 크리티컬 패스만 검증.
* **회귀 테스트 강제:** 버그 발생 시 반드시 **해당 계층의 테스트로 버그 상황을 재현**하여 실패(Red)를 확인한 후 코드를 수정(Green)합니다.

### 10.2 테스트 격리 (Test Isolation)
`[Operational Safeguard]`
* 각 테스트는 실행 순서에 의존하지 않아야 합니다. 공유 상태(DB, Redis)는 매 테스트 전/후로 완벽히 초기화(`@Sql`, `Testcontainers`)되어야 합니다.

### 10.3 ArchUnit 아키텍처 강제
`[Hexagonal Architecture 규칙]`
멀티 모듈을 사용하지 않는 단일 모듈 프로젝트의 경우, CI 파이프라인에서 아래 규칙 위반 시 빌드는 실패합니다.
* `domain` 계층은 `infrastructure`, `presentation`, `application` 계층을 절대 참조(import)할 수 없습니다.
* `domain` 내부 클래스에 Spring, jOOQ 등 외부 기술 어노테이션 사용을 금지합니다.

---

## 11. PR Review Checklist
모든 개발자는 PR 생성 시 아래 체크리스트를 점검해야 합니다.

- [ ] **`[Hexagonal]` Domain 순수성:** `domain` 패키지(또는 `boilerplate-domain` 모듈)에 jOOQ Record, Spring API `import`가 없는가?
- [ ] **`[Pragmatic]` 불필요한 매핑 방지:** 단순 DTO 전달을 위해 과도한 인터페이스(Port)를 만들지 않았는가?
- [ ] **`[DDD]` Application 책무:** Application Service 내부에 `if/else`로 비즈니스를 통제하는 핵심 로직이 없는가?
- [ ] **`[DDD]` Aggregate 통제:** Repository가 오직 Aggregate Root 단위로만 작성되었는가?
- [ ] **`[CQRS]` 조회 분리:** 단순 조회를 위해 Domain Entity를 로드하지 않고, QueryService에서 DTO로 직접 맵핑했는가?
- [ ] **`[Ops]` 트랜잭션/동시성:** 1 트랜잭션 = 1 Aggregate 원칙 준수 및 낙관적 락 로직이 포함되었는가?
- [ ] **`[Ops]` 이벤트 순서 & 멱등성:** Aggregate ID 기반 파티셔닝이 적용되었으며, 중복 수신 시에도 데이터가 보호되는가?
- [ ] **`[Ops]` 예외 처리 & DLQ:** 실패 가능성이 있는 이벤트 처리에 Retry 및 DLQ 정책이 반영되었는가?

---

## 아키텍처 11대 절대 헌법 (The 11 Golden Rules)
1. **모든 비즈니스 로직은 Domain에 존재한다.**
2. **Application은 판단하지 않고 오직 오케스트레이션만 제어한다.**
3. `[Hexagonal]` **Domain은 어떤 외부 기술(프레임워크, DB)도 몰라야 한다.**
4. **Aggregate는 데이터 일관성을 지키는 최소 단위이자 트랜잭션의 경계다.**
5. **Aggregate 간 객체 직접 참조 및 직접 트랜잭션 결합을 엄격히 금지한다.**
6. **Bounded Context 간의 소스 코드 직접 참조는 금지한다.**
7. `[Hexagonal]` **JPA를 배제하고 jOOQ를 사용하며, Domain 모델과 DB Record 객체는 인프라 계층에서 수동 매핑한다.**
8. **조회(Query) 로직은 CQRS를 통해 도메인을 우회하여 즉시 DTO로 변환한다. (단순 CRUD 예외)**
9. **도메인 이벤트는 유실되지 않아야(Outbox) 하며, 순서가 보장되고 처리는 멱등해야 한다.**
10. **시스템의 모든 행위는 추적 가능해야 하며(Trace ID), 실패는 격리되어야 한다(DLQ).**
11. **버그는 코드를 수정하기 전 단위 테스트로 먼저 증명해야 한다.**
