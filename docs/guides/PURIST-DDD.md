# Purist DDD & Hexagonal Architecture Constitution (v1.0)

**"비즈니스의 의도는 도메인(DDD)으로 빠짐없이 모델링하고, 기술의 오염은 헥사고날(Hexagonal)로 완벽히 차단하며, 어떠한 편의적 타협도 허용하지 않는다. 순수성이 곧 장기적 생산성이다."**

## 1. 개요 (Introduction)

본 문서는 도메인 주도 설계와 헥사고날 아키텍처를 **원전(原典) 그대로** 적용하기 위한 아키텍처 표준입니다.

실용주의 아키텍처가 "보일러플레이트 절감"을 명분으로 계층 간 경계를 흐리는 것과 달리, 본 헌법은 **모든 계층 간 통신에 명시적 Port/Adapter를 강제**하고, **도메인 모델의 어떠한 누수(Leaky Abstraction)도 용납하지 않습니다.**

근거:
- Alistair Cockburn의 원본 Hexagonal Architecture(Ports & Adapters) 논문
- Eric Evans의 Domain-Driven Design(2003) 원전
- Vaughn Vernon의 Implementing Domain-Driven Design(2013)

**핵심 철학:** 보일러플레이트는 삭제해야 할 비용이 아니라, 아키텍처 경계를 물리적으로 증명하는 **계약(Contract)**이다.

---

## 2. 전략적 설계와 경계 관리 (Strategic Design)

### 2.1 바운디드 컨텍스트 (Bounded Context)
바운디드 컨텍스트는 단순한 패키지 분리가 아닌 **완벽한 독립 시스템**으로 간주합니다.

* **직접 참조 금지:** 다른 Context의 Domain 객체(Entity, VO, DB Record, DTO)를 절대 `import` 하지 않습니다.
* **데이터 전달:** Context 간 데이터 전달은 오직 **Published Language(공개 언어, 별도 공유 스키마 모듈)** 또는 **도메인 이벤트(비동기 메시징)**로만 수행합니다.
* **Anti-Corruption Layer 강제:** 외부 Context로부터 수신한 데이터는 반드시 ACL을 통해 자신의 Ubiquitous Language로 번역되어야 합니다. 외부 모델이 내부 도메인에 직접 진입하는 것을 금지합니다.

### 2.2 컨텍스트 매핑 (Context Mapping)
모든 바운디드 컨텍스트 간의 관계는 명시적 컨텍스트 맵으로 문서화되어야 합니다.

* 허용되는 관계 패턴: Partnership, Shared Kernel, Customer-Supplier, Conformist, ACL, Open Host Service, Published Language, Separate Ways
* **Big Ball of Mud 관계는 절대 허용하지 않습니다.** 레거시 시스템과의 통합이 불가피한 경우에도 반드시 ACL을 배치합니다.

---

## 3. 패키지 및 멀티 모듈 구조 표준 (Structure Standard)

### 3.1 멀티 모듈은 유일한 선택지

단일 모듈 구조는 **허용하지 않습니다.** 컴파일 타임에 아키텍처 위반을 원천 차단하려면 물리적 모듈 분리가 유일한 수단입니다. ArchUnit 같은 런타임 검사 도구는 보조 수단일 뿐, 1차 방어선이 될 수 없습니다.

### 3.2 멀티 모듈 표준 (Multi-Module Layout)

```text
boilerplate/                                        # Root Project
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
│
│ ── [Hexagon Core] boilerplate-domain ──────────────────────
│    순수 비즈니스 모듈 — 외부 의존성 0%, 예외 없음
│    허용: 순수 Java/Kotlin만
│    금지: Spring, jOOQ, Jackson, Lombok, jakarta.validation,
│           어떠한 프레임워크/라이브러리 어노테이션도 금지
│
├── boilerplate-domain/
│   ├── build.gradle.kts                          # dependencies {} 블록 비어 있어야 함
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── domain/
│           ├── model/                            # Aggregate Root, Entity, Value Object
│           │   ├── Tenant.java                   # Aggregate Root (불변 POJO, 자체 검증)
│           │   ├── TenantId.java                 # VO — 식별자 (UUID 래퍼)
│           │   ├── TenantStatus.java             # VO — enum
│           │   ├── TenantName.java               # VO — 자체 검증 포함 (null, 길이 등)
│           │   └── OwnerId.java                  # VO — 다른 Aggregate의 ID 참조
│           │
│           ├── service/                          # Domain Service
│           │   └── TenantDomainService.java      # 여러 Aggregate 간 순수 비즈니스 조율
│           │
│           ├── event/                            # Domain Event
│           │   ├── DomainEvent.java              # 마커 인터페이스
│           │   ├── TenantCreatedEvent.java       # 불변 record
│           │   └── TenantSuspendedEvent.java
│           │
│           └── exception/                        # Domain Exception
│               ├── DomainException.java          # 추상 베이스 (HTTP/기술 의존 절대 금지)
│               ├── TenantNotFoundException.java
│               └── TenantAlreadyExistsException.java
│
│ ── [Application Core] boilerplate-application ─────────────
│    유스케이스 모듈 — Port 정의 + Application Service
│    의존성: boilerplate-domain만 허용
│    금지: Spring, jOOQ, 어떠한 인프라 기술도 금지
│
├── boilerplate-application/
│   ├── build.gradle.kts                          # implementation(project(":boilerplate-domain"))만
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── application/
│           ├── port/
│           │   ├── input/                        # Input Port (Use Case 인터페이스) — 필수
│           │   │   ├── CreateTenantUseCase.java   # 모든 유스케이스는 반드시 인터페이스
│           │   │   ├── SuspendTenantUseCase.java
│           │   │   ├── GetTenantUseCase.java      # 조회도 예외 없이 Port 정의
│           │   │   └── ListTenantsUseCase.java
│           │   │
│           │   └── output/                       # Output Port — 필수
│           │       ├── LoadTenantPort.java         # 조회 전용
│           │       ├── SaveTenantPort.java         # 저장 전용
│           │       ├── TenantEventPublisher.java   # 이벤트 발행
│           │       ├── TenantQueryPort.java        # CQRS 조회
│           │       └── ExternalIdpClient.java      # 외부 시스템 연동
│           │
│           ├── service/                          # Application Service (Input Port 구현체)
│           │   ├── CreateTenantService.java       # implements CreateTenantUseCase
│           │   ├── SuspendTenantService.java      # implements SuspendTenantUseCase
│           │   ├── GetTenantService.java          # implements GetTenantUseCase
│           │   └── ListTenantsService.java        # implements ListTenantsUseCase
│           │
│           └── dto/                              # Application DTO (계층 간 전달 전용)
│               ├── command/                      # Command DTO
│               │   ├── CreateTenantCommand.java   # 불변 record
│               │   └── SuspendTenantCommand.java
│               ├── query/                        # Query DTO
│               │   ├── TenantInfo.java
│               │   └── TenantSummary.java
│               └── result/                       # Result DTO
│                   └── TenantResult.java          # 유스케이스 실행 결과
│
│ ── [Inbound Adapter] boilerplate-adapter-in-web ───────────
│    REST API 어댑터 — Controller만 위치
│    의존성: boilerplate-application만 허용 (domain 직접 참조 금지)
│    역할: HTTP 요청 → Command/Query 변환 → Input Port 호출 → 응답 변환
│
├── boilerplate-adapter-in-web/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── adapter/
│           └── in/
│               └── web/
│                   ├── TenantController.java      # Input Port(UseCase)에만 의존
│                   ├── TenantAdminController.java
│                   ├── dto/
│                   │   ├── request/
│                   │   │   ├── CreateTenantRequest.java   # Web 전용 DTO
│                   │   │   └── UpdateTenantRequest.java
│                   │   └── response/
│                   │       ├── TenantResponse.java
│                   │       └── TenantListResponse.java
│                   ├── mapper/                    # Web DTO ↔ Application Command/Query 변환
│                   │   └── TenantWebMapper.java   # 명시적 매핑 필수
│                   └── advice/
│                       └── GlobalExceptionHandler.java
│
│ ── [Inbound Adapter] boilerplate-adapter-in-messaging ─────
│    메시지 수신 어댑터 — Consumer만 위치
│    의존성: boilerplate-application만 허용
│
├── boilerplate-adapter-in-messaging/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── adapter/
│           └── in/
│               └── messaging/
│                   ├── TenantEventConsumer.java    # 메시지 → Command 변환 → Input Port 호출
│                   └── mapper/
│                       └── TenantMessageMapper.java
│
│ ── [Outbound Adapter] boilerplate-adapter-out-persistence-jooq ────
│    jOOQ 영속성 어댑터 — Output Port 구현체
│    의존성: boilerplate-application + boilerplate-domain
│    금지: Spring Web, 다른 adapter 모듈 직접 참조
│
├── boilerplate-adapter-out-persistence-jooq/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/io/github/ppzxc/boilerplate/
│       │   └── adapter/
│       │       └── out/
│       │           └── persistence/
│       │               ├── TenantPersistenceAdapter.java
│       │               │   # implements LoadTenantPort, SaveTenantPort
│       │               │   # 하나의 Port = 하나의 Adapter (SRP)
│       │               │
│       │               ├── TenantQueryAdapter.java
│       │               │   # implements TenantQueryPort
│       │               │
│       │               ├── mapper/
│       │               │   └── TenantPersistenceMapper.java
│       │               │       # Domain Model ↔ jOOQ Record 수동 매핑
│       │               │       # 양방향: toDomain(), toRecord()
│       │               │
│       │               ├── outbox/
│       │               │   ├── OutboxEntry.java
│       │               │   ├── OutboxPersistenceAdapter.java
│       │               │   └── OutboxRelayScheduler.java
│       │               │
│       │               └── config/
│       │                   ├── JooqConfig.java
│       │                   └── FlywayConfig.java
│       │
│       └── main/resources/
│           └── db/migration/
│               ├── V001__create_tenant.sql
│               ├── V002__create_outbox.sql
│               └── V003__add_version_column.sql
│
│ ── [Outbound Adapter] boilerplate-adapter-out-messaging-{broker} ──
│    메시지 발행 어댑터
│    의존성: boilerplate-application + boilerplate-domain
│
├── boilerplate-adapter-out-messaging-rabbitmq/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── adapter/
│           └── out/
│               └── messaging/
│                   ├── TenantEventPublisherAdapter.java
│                   │   # implements TenantEventPublisher
│                   └── mapper/
│                       └── TenantEventMessageMapper.java
│
│ ── [Outbound Adapter] boilerplate-adapter-out-external-idp ────
│    외부 IdP 연동 어댑터
│    의존성: boilerplate-application
│
├── boilerplate-adapter-out-external-idp/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       └── adapter/
│           └── out/
│               └── external/
│                   └── idp/
│                       ├── IdpClientAdapter.java
│                       │   # implements ExternalIdpClient
│                       ├── mapper/
│                       │   └── IdpResponseMapper.java
│                       └── config/
│                           └── IdpClientConfig.java
│
│ ── [Configuration] boilerplate-configuration ──────────────
│    Spring Boot 실행 + DI 조립 전용
│    모든 모듈을 의존하여 Bean 와이어링만 수행
│    비즈니스 로직 절대 금지
│
├── boilerplate-configuration/
│   ├── build.gradle.kts                          # 모든 adapter 모듈 의존
│   └── src/main/java/io/github/ppzxc/boilerplate/
│       ├── BoilerplateApplication.java           # @SpringBootApplication
│       └── config/
│           ├── BeanConfiguration.java            # 수동 Bean 등록 (자동 스캔 최소화)
│           ├── WebConfig.java
│           ├── SecurityConfig.java
│           └── CorsConfig.java
│
│ ── [Support] boilerplate-common ───────────────────────
│    공통 유틸리티 — 에러 코드, Trace ID
│    의존성: 순수 Java만
│
└── boilerplate-common/
    ├── build.gradle.kts
    └── src/main/java/io/github/ppzxc/boilerplate/
        └── common/
            ├── error/
            │   ├── ErrorCode.java
            │   └── ErrorResponse.java
            ├── trace/
            │   └── TraceContext.java
            └── page/
                └── CursorPageResult.java
```

### 3.3 의존성 방향 매트릭스 (Dependency Direction Matrix)

| 모듈 | 허용되는 의존 대상 | 금지 |
|---|---|---|
| `domain` | 없음 (순수 Java만) | 모든 외부 라이브러리 |
| `application` | `domain` | Spring, jOOQ, 모든 기술 |
| `adapter-in-*` | `application` | `domain` 직접 참조, 다른 adapter |
| `adapter-out-*` | `application`, `domain` | Spring Web, 다른 adapter |
| `configuration` | 모든 모듈 | 비즈니스 로직 |
| `common` | 없음 (순수 Java만) | 모든 외부 라이브러리 |

* **[CRITICAL] Adapter 간 순환 참조 절대 금지:** `adapter-*` 모듈 간의 어떠한 직접 참조도 금지됩니다.
* **[CRITICAL] Inbound Adapter → Domain 직접 참조 금지:** Inbound Adapter는 Application 계층의 Port(UseCase 인터페이스)에만 의존하며, Domain 모델을 직접 참조할 수 없습니다.

---

## 4. 계층형 아키텍처 통제 (Layered Control)

### 4.1 Domain Layer (도메인 계층)

도메인 계층은 시스템의 심장이며, 어떠한 기술적 타협도 허용하지 않습니다.

* **[ABSOLUTE] 외부 의존성 제로:** `build.gradle.kts`의 `dependencies {}` 블록은 비어 있어야 합니다. `jakarta.validation`도 금지합니다. 모든 검증은 생성자 또는 팩토리 메서드에서 순수 Java로 수행합니다.
* **[ABSOLUTE] 프레임워크 어노테이션 금지:** `@Entity`, `@Component`, `@Valid`, `@JsonProperty` 등 어떠한 어노테이션도 도메인 클래스에 부착할 수 없습니다.
* **Rich Domain Model 강제:** 모든 비즈니스 규칙은 Aggregate Root 또는 Entity의 행위 메서드 내부에 캡슐화됩니다. Getter/Setter만 있는 Anemic Domain Model은 아키텍처 위반입니다.
* **자체 검증 (Self-Validating):** Value Object와 Entity는 생성 시점에 불변식(Invariant)을 스스로 검증합니다. 유효하지 않은 상태의 객체는 존재할 수 없습니다.
* **도메인 이벤트 수집:** Aggregate Root는 내부에 이벤트 목록을 유지하고, 행위 메서드 실행 시 이벤트를 등록합니다. Application Service가 저장 후 이벤트를 수거하여 발행합니다.

```java
// 도메인 순수성 예시 — 어떠한 어노테이션도 없음
public final class Tenant {
    private final TenantId id;
    private TenantName name;
    private TenantStatus status;
    private final OwnerId ownerId;
    private final long version;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // private 생성자 — 팩토리 메서드를 통해서만 생성
    private Tenant(TenantId id, TenantName name, TenantStatus status,
                   OwnerId ownerId, long version) {
        this.id = Objects.requireNonNull(id, "TenantId must not be null");
        this.name = Objects.requireNonNull(name, "TenantName must not be null");
        this.status = Objects.requireNonNull(status, "TenantStatus must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId must not be null");
        this.version = version;
    }

    public static Tenant create(TenantName name, OwnerId ownerId) {
        var tenant = new Tenant(
            TenantId.generate(), name, TenantStatus.ACTIVE, ownerId, 0L
        );
        tenant.registerEvent(new TenantCreatedEvent(tenant.id, name));
        return tenant;
    }

    // 비즈니스 행위 — 도메인 로직이 여기에 캡슐화
    public void suspend(String reason) {
        if (this.status != TenantStatus.ACTIVE) {
            throw new TenantNotActiveException(this.id);
        }
        this.status = TenantStatus.SUSPENDED;
        registerEvent(new TenantSuspendedEvent(this.id, reason));
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // ID 참조만 허용 — 다른 Aggregate 객체 직접 참조 금지
    public OwnerId ownerId() { return this.ownerId; }
    public TenantId id() { return this.id; }
    public long version() { return this.version; }
}
```

### 4.2 Application Layer (응용 계층)

Application Service는 오케스트레이터(지휘자) 역할**만** 수행하며, 반드시 Input Port를 구현합니다.

* **[ABSOLUTE] Input Port 인터페이스 필수:** 모든 유스케이스는 반드시 `port/input/` 패키지에 인터페이스로 먼저 정의됩니다. "간단한 CRUD라서 생략"은 허용하지 않습니다.
* **[ABSOLUTE] Output Port 인터페이스 필수:** 모든 외부 자원 접근(DB, 메시지, HTTP)은 반드시 `port/output/` 패키지에 인터페이스로 먼저 정의됩니다.
* **[FORBIDDEN] 금지 사항:**
    * 비즈니스 정책 판단 (if/else로 비즈니스 룰 분기)
    * 반복문 내부에서의 복잡한 도메인 계산
    * 기술 키워드 사용 (`DSLContext`, `WebClient`, `RestTemplate`, `ObjectMapper` 등)
    * Presentation DTO 직접 참조 (Request/Response DTO가 Application 계층에 진입하는 것 금지)
* **[ALLOWED] 허용되는 단 4가지 역할:**
    1. **입력 변환:** Command/Query DTO를 도메인 객체로 변환
    2. **조회:** Output Port를 통한 Aggregate 로드
    3. **위임:** 도메인 객체의 행위(메서드) 호출
    4. **저장/발행:** 상태 변경 완료 후 Output Port를 통한 저장 및 이벤트 발행

```java
// Input Port — 반드시 인터페이스로 정의
public interface CreateTenantUseCase {
    TenantResult execute(CreateTenantCommand command);
}

// Application Service — Input Port 구현
public class CreateTenantService implements CreateTenantUseCase {
    private final LoadTenantPort loadTenantPort;
    private final SaveTenantPort saveTenantPort;
    private final TenantEventPublisher eventPublisher;

    @Override
    public TenantResult execute(CreateTenantCommand command) {
        // 1. Command → Domain 변환
        var name = new TenantName(command.name());
        var ownerId = new OwnerId(command.ownerId());

        // 2. 도메인 행위 위임
        var tenant = Tenant.create(name, ownerId);

        // 3. 저장
        saveTenantPort.save(tenant);

        // 4. 이벤트 발행
        tenant.pullDomainEvents().forEach(eventPublisher::publish);

        // 5. 결과 변환 (Application DTO)
        return new TenantResult(tenant.id().value(), tenant.name().value());
    }
}
```

### 4.3 Inbound Adapter Layer (인바운드 어댑터 계층)

* **[ABSOLUTE] Input Port에만 의존:** Controller는 `CreateTenantUseCase` 같은 인터페이스 타입만 주입받습니다. `CreateTenantService` 같은 구현체 타입을 직접 참조하면 아키텍처 위반입니다.
* **[ABSOLUTE] 매핑 필수:** Web Request DTO → Application Command DTO 변환은 반드시 명시적 Mapper를 통해 수행합니다. Presentation DTO를 Application 계층에 그대로 전달하는 것은 금지됩니다.
* **[ABSOLUTE] Domain 모델 직접 참조 금지:** Inbound Adapter는 `domain` 패키지의 어떤 클래스도 `import`할 수 없습니다. 오직 `application` 패키지의 Port, Command, Query, Result DTO만 사용합니다.

```java
@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    // UseCase 인터페이스에만 의존 — 구현체 모름
    private final CreateTenantUseCase createTenantUseCase;
    private final TenantWebMapper mapper;

    @PostMapping
    public ResponseEntity<TenantResponse> create(
            @RequestBody @Valid CreateTenantRequest request) {
        // Web DTO → Application Command (명시적 매핑)
        var command = mapper.toCommand(request);
        // Input Port 호출
        var result = createTenantUseCase.execute(command);
        // Application Result → Web Response (명시적 매핑)
        return ResponseEntity.created(/*...*/).body(mapper.toResponse(result));
    }
}
```

### 4.4 Outbound Adapter Layer (아웃바운드 어댑터 계층)

* **Output Port 구현체만 위치:** 각 Adapter 클래스는 하나의 Output Port만 구현합니다 (SRP 엄격 적용).
* **기술 격리:** jOOQ, RabbitMQ, HTTP Client 등의 기술적 코드는 이 계층에서만 존재합니다.
* **양방향 수동 매핑:** Domain Model ↔ 기술 데이터 구조(jOOQ Record, JSON 등)의 변환은 명시적 Mapper 클래스를 통해 수행합니다.

### 4.5 Configuration Layer (설정 계층)

* **조립만 수행:** Spring Boot 실행, Bean 와이어링, 프로파일 설정만 수행합니다.
* **비즈니스 로직 절대 금지:** 어떠한 도메인 로직, 유틸리티 로직도 이 모듈에 위치할 수 없습니다.
* **수동 Bean 등록 권장:** `@ComponentScan`의 범위를 최소화하고, `@Configuration` + `@Bean` 메서드를 통한 명시적 의존성 조립을 권장합니다. Port-Adapter 관계가 코드에서 명확히 보여야 합니다.

---

## 5. 전술적 설계와 Aggregate (Tactical Design)

### 5.1 Aggregate 설계 원칙

* **크기 최소화:** Aggregate는 가능한 작게 유지하여 락(Lock) 경합을 줄입니다.
* **ID 참조 강제:** 다른 Aggregate를 참조할 때는 **반드시** ID 참조만 사용합니다. 객체 참조는 아키텍처 위반입니다.
* **트랜잭션 경계:** **1 Transaction = 1 Aggregate. 예외 없음.** 여러 Aggregate의 변경이 필요하다면 반드시 도메인 이벤트를 발행하여 Eventually Consistent하게 처리합니다.
* **식별자 사전 생성:** 모든 Aggregate Root는 DB Auto Increment에 의존하지 않고, **생성 시점에 UUID/ULID 식별자**를 부여합니다.
* **불변식 보호:** Aggregate Root는 자신이 관리하는 모든 Entity와 Value Object의 불변식(Invariant)을 보호할 책임이 있습니다. 외부에서 내부 Entity를 직접 변경할 수 없습니다.

### 5.2 Value Object 설계 원칙

* **불변(Immutable):** 모든 VO는 생성 후 변경 불가합니다.
* **자체 검증:** 유효하지 않은 값으로 VO를 생성할 수 없습니다.
* **동등성(Equality):** VO의 동등성은 값(속성)으로만 판단합니다. (`equals`/`hashCode` 오버라이드 필수)
* **원시 타입 포장(Primitive Obsession 금지):** 도메인에서 의미를 가지는 모든 값은 VO로 포장합니다. `String tenantName`이 아닌 `TenantName tenantName`을 사용합니다.

```java
// Value Object — 불변, 자체 검증, 동등성
public record TenantName(String value) {
    public TenantName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tenant name must not be blank");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Tenant name must not exceed 100 characters");
        }
    }
}
```

### 5.3 Domain Service 사용 기준

Domain Service는 **단일 Aggregate로 표현할 수 없는** 비즈니스 로직에만 사용합니다.

* 여러 Aggregate의 **읽기 전용** 정보를 참조하여 비즈니스 판단을 내리는 경우
* 특정 Aggregate에 자연스럽게 속하지 않는 도메인 규칙
* **금지:** Domain Service가 직접 Repository를 호출하는 것은 허용하지 않습니다. 필요한 데이터는 Application Service가 로드하여 전달합니다.

### 5.4 Repository 인터페이스 설계

* **Aggregate Root 단위만:** Repository는 오직 Aggregate Root에 대해서만 정의합니다.
* **도메인 언어 사용:** 메서드명은 기술이 아닌 도메인 언어로 작성합니다.
    * **GOOD:** `findByTenantId(TenantId id)`, `save(Tenant tenant)`
    * **BAD:** `selectById(Long id)`, `insertOrUpdate(TenantRecord record)`
* **컬렉션 시맨틱:** Repository는 도메인 관점에서 컬렉션(Collection)처럼 보여야 합니다.
* **Repository와 Query Port 분리:** Command용 Repository(`domain` 패키지)와 Query용 Port(`application/port/output` 패키지)는 반드시 분리합니다. 하나의 인터페이스가 두 역할을 겸하는 것을 금지합니다.

---

## 6. 100% jOOQ 영속성 및 CQRS 전략

JPA를 배제하고 jOOQ를 사용하여 데이터베이스 통제권을 확보합니다. 단, 어떠한 "편의적 지름길"도 허용하지 않습니다.

### 6.1 Command (상태 변경 로직)

* **Domain 순수성:** `domain` 내부에 jOOQ의 `Table`, `Record`, `DSLContext` 관련 코드가 절대 침투해서는 안 됩니다.
* **수동 매핑:** Adapter의 `PersistenceMapper`가 Domain Model ↔ jOOQ Record 양방향 변환을 전담합니다.
* **Aggregate 단위 저장:** Aggregate Root와 하위 Entity를 하나의 트랜잭션으로 저장합니다.

### 6.2 Query (조회 로직)

* **도메인 우회 (CQRS):** 화면 조회를 위해 Domain Entity를 거치지 않습니다.
* **직접 프로젝션:** `TenantQueryAdapter`(implements `TenantQueryPort`)에서 jOOQ Type-Safe SQL을 작성하고, Application DTO로 직접 매핑합니다.
* **[CRITICAL] 조회도 반드시 Port를 경유:** 조회 로직이라 하더라도, Application Service는 `TenantQueryPort` 인터페이스에 의존합니다. jOOQ `DSLContext`를 직접 사용하는 것은 금지됩니다.

### 6.3 단순 CRUD 예외 정책 — 없음

**실용주의 헌법과의 핵심 차이점입니다.**

단순 CRUD라는 이유로 도메인 계층을 우회하는 것을 **허용하지 않습니다.** 모든 데이터 접근은 예외 없이 Application Service → Port → Adapter 경로를 따릅니다.

* Read-only 조회, 통계 화면 → 반드시 `QueryPort`를 통해 접근
* 마스터 코드 관리, 설정값 CRUD → 반드시 해당 Aggregate의 `UseCase` Port를 통해 접근
* "오늘 단순 CRUD인 것이 내일은 복잡한 비즈니스가 된다"는 가정 하에 설계합니다.

### 6.4 동시성 제어 (Concurrency Control)

* Aggregate Root 업데이트 시 낙관적 락(Optimistic Lock)을 강제합니다.
* Version 필드는 Domain Model에 위치하며, Adapter가 jOOQ `recordVersionFields`를 활용하여 충돌을 감지합니다.

---

## 7. 매핑 전략 (Mapping Strategy)

실용주의 아키텍처가 "불필요한 매핑 생략"을 허용하는 것과 달리, 본 헌법은 **모든 계층 경계에서 명시적 매핑을 강제**합니다.

### 7.1 전체 계층 매핑 흐름

```
[HTTP Request]
    ↓ TenantWebMapper.toCommand()
[CreateTenantCommand]  (Application DTO)
    ↓ Application Service 내부에서 도메인 객체 생성
[Tenant]  (Domain Model)
    ↓ TenantPersistenceMapper.toRecord()
[TenantRecord]  (jOOQ Record)
    ↓ DB 저장 후 조회
[TenantRecord]  (jOOQ Record)
    ↓ TenantPersistenceMapper.toDomain()
[Tenant]  (Domain Model)
    ↓ Application Service에서 Result DTO 생성
[TenantResult]  (Application DTO)
    ↓ TenantWebMapper.toResponse()
[TenantResponse]  (Web DTO)
    ↓
[HTTP Respons

### 7.2 매핑 원칙

* **각 계층은 자신만의 데이터 구조를 갖습니다:** Web DTO, Application Command/Query/Result DTO, Domain Model, jOOQ Record — 4종의 데이터 구조가 독립적으로 존재합니다.
* **매핑 코드는 경계를 넘는 쪽(Adapter)에 위치합니다.**
* **MapStruct 등 자동 매핑 도구 사용 허용:** 단, 매핑 인터페이스는 명시적으로 정의되어야 하며, 암묵적 필드명 매칭에 의존하는 것을 지양합니다.
* **매핑 누ë´파일 에러로 감지:** 새 필드 추가 시 매핑 코드에서 컴파일 에러가 발생하는 구조를 지향합니다.

---

## 8. 도메인 이벤트와 신뢰성 (Event Reliability)

분산 환경에서 데이터 정합성을 지키는 핵심 생명선입니다.

* **발행 보장 (Outbox Pattern, At-Least-Once):** 비즈니스 상태 변경과 이벤트 기록을 동일한 로컬 DB 트랜잭션으로 묶어 저장합니다. 릴레이 워커가 메시지 브로커로 전송하여 유실을 ë¤. `SELECT ... FOR UPDATE SKIP LOCKED`로 폴링 경합을 방지합니다.
* **순서 보장 (Event Ordering):** 동일 Aggregate에서 발생한 이벤트는 **Aggregate ID 기반 파티셔닝**으로 순서를 보장합니다.
* **멱등성 (Idempotency):** 모든 이벤트 컨슈머는 중복 수신에 대해 멱등하게 동작해야 합니다.
* **장애 복구 (Retry & DLQ):** Exponential Backoff + DLQ 격리 + 모니터링 알람 필수.
* **이벤트 스키마 버저닝:** 이벤트 스키마 변해야 합니다. 필드 삭제는 deprecation 기간을 거쳐 수행합니다.

---

## 9. 운영 관측성 및 스키마 관리 (Observability & Schema)

* **추적성 (Tracing):** 모든 외부 API 요청, 내부 도메인 처리, 발행된 이벤트에는 동일한 **Correlation ID(Trace ID)**가 포함되어야 합니다.
* **스키마 관리:** 모든 DB 변경은 `Flyway`로만 관리합니다. jOOQ Codegen은 마이그레이션이 끝난 스키마를 기반으로 실행합니다.
* **메트릭:** ê간, 성공/실패 횟수를 메트릭으로 노출합니다.
* **헬스체크:** 모든 Outbound Adapter(DB, 메시지 브로커, 외부 API)에 대한 헬스체크를 구현합니다.

---

## 10. 예외 처리 원칙 (Exception Policy)

* **Domain 예외:** `domain`은 오직 비즈니스 예외만 발생시킵니다. HTTP 기술 의존성 절대 금지.
* **Infra 예외 변환:** 기술 예외는 반드시 Adapter 계층에서 Catch하여 의미 있는 도메인 예외 또는 Application 예외로 ëlication/Domain 계층까지 전파되는 것을 금지합니다.
* **표준 에러 규격:** 외부로 나가는 모든 예외는 `@ControllerAdvice`를 통해 RFC 9457 Problem Details 형식으로 변환됩니다.
* **예외 계층 구조:**
    * `DomainException` (domain 모듈) — 비즈니스 규칙 위반
    * `ApplicationException` (application 모듈) — 유스케이스 실행 실패
    * `InfrastructureException` (adapter 모듈) — 기술 오류 래핑

---

## 11. 테스트 품질 및 강(Test & Enforcement)

### 11.1 테스트 전략 비율

* **[70%] Domain Test:** 외부 프레임워크 없는 순수 Java 단위 테스트. Aggregate의 모든 행위, 불변식, 이벤트 발행을 검증합니다.
* **[15%] Application Test:** Mocking을 활용하여 오케스트레이션 흐름(Port 호출 순서, 이벤트 수거/발행)을 검증합니다. Input Port 인터페이스를 통해 테스트합니다.
* **[10%] Adapter Integration Test:** Testcontainers로 실제 DB/브로커를 띄워 매í©성, 낙관적 락 충돌, Outbox 저장/폴링을 검증합니다.
* **[5%] E2E Test:** Configuration 모듈에서 전체 조립 후 핵심 크리티컬 패스만 검증합니다.
* **회귀 테스트 강제:** 버그 발생 시 해당 계층의 테스트로 버그를 재현(Red)한 후 코드를 수정(Green)합니다.

### 11.2 테스트 격리 (Test Isolation)

* 각 테스트는 실행 순서에 의존하지 않습니다. 공유 상태는 매 테스트 전/후로 완벽히 초기화합니다.
* **Doin 테스트에 Spring Context를 사용하면 아키텍처 위반입니다.** `@SpringBootTest`는 E2E 테스트에서만 허용합니다.

### 11.3 아키텍처 강제 (Architecture Enforcement)

* **1차 방어선: 멀티 모듈.** 물리적 모듈 분리로 컴파일 타임에 위반을 차단합니다.
* **2차 방어선: ArchUnit.** 모듈 내부의 패키지 간 의존성을 검증합니다.
* **CI 파이프라인:** ArchUnit 테스트 실패 시 빌드가 실패합니다.

---

## 12. PR Review Chec
모든 개발자는 PR 생성 시 아래 체크리스트를 점검해야 합니다.

- [ ] **`[Hexagonal]` Domain 순수성:** `domain` 모듈에 어떠한 외부 라이브러리 의존성도 없는가? (`build.gradle.kts` 확인)
- [ ] **`[Hexagonal]` Port 완전성:** 새로운 유스케이스에 Input Port 인터페이스가 정의되었는가?
- [ ] **`[Hexagonal]` Port 완전성:** 새로운 외부 자원 접근에 Output Port 인터페이스가 정의되었는가?
- [ ] **`[Hexagonal]` Adapter 격리und Adapter가 Domain 모듈을 직접 참조하지 않는가?
- [ ] **`[Hexagonal]` Adapter 격리:** Adapter 모듈 간 직접 참조가 없는가?
- [ ] **`[Mapping]` 계층 간 매핑:** 모든 계층 경계에서 명시적 DTO 변환이 수행되는가?
- [ ] **`[DDD]` Rich Domain Model:** 비즈니스 로직이 Domain Model 내부에 캡슐화되었는가? (Anemic Model 금지)
- [ ] **`[DDD]` Value Object:** 원시 타입이 도메인 의미를 가질 때 VO로 포장되었는가?
- [ ] **`[DDD]` Apption 책무:** Application Service 내부에 비즈니스 판단 로직이 없는가?
- [ ] **`[DDD]` Aggregate 통제:** Repository가 오직 Aggregate Root 단위로만 작성되었는가?
- [ ] **`[CQRS]` 조회 분리:** 모든 조회가 QueryPort를 통해 수행되는가? (직접 CRUD 우회 금지)
- [ ] **`[Ops]` 트랜잭션/동시성:** 1 트랜잭션 = 1 Aggregate 원칙 준수 및 낙관적 락 포함되었는가?
- [ ] **`[Ops]` 이벤트:** Aggregate ID 기반 파티셔닝, 멱등성, Retry/DL
- [ ] **`[Test]` 테스트 커버리지:** Domain 테스트가 Spring 없이 순수 Java로 작성되었는가?

---

## 아키텍처 13대 절대 헌법 (The 13 Golden Rules)

1. **모든 비즈니스 로직은 Domain에 존재한다. Rich Domain Model을 강제한다.**
2. **Application은 판단하지 않고 오직 오케스트레이션만 제어한다.**
3. **Domain은 어떤 외부 기술(프레임워크, DB, 직렬화)도 몰라야 한다. `dependencies {}` = 비어 있음.**
4. **모든 유스케이t 인터페이스로 정의된다. 예외 없음.**
5. **모든 외부 자원 접근은 Output Port 인터페이스로 정의된다. 예외 없음.**
6. **Inbound Adapter는 Application Port에만 의존한다. Domain 직접 참조를 금지한다.**
7. **Aggregate는 데이터 일관성을 지키는 최소 단위이자 트랜잭션의 경계다. 1 Transaction = 1 Aggregate.**
8. **Aggregate 간 객체 직접 참조 및 직접 트랜잭션 결합을 엄격히 금지한다.**
9. **Bounded Context 간의 소ì 직접 참조를 금지한다. ACL을 강제한다.**
10. **모든 계층 경계에서 명시적 매핑을 수행한다. Presentation DTO가 Application 계층에 진입하는 것을 금지한다.**
11. **도메인 이벤트는 유실되지 않아야(Outbox) 하며, 순서가 보장되고 처리는 멱등해야 한다.**
12. **시스템의 모든 행위는 추적 가능해야 하며(Trace ID), 실패는 격리되어야 한다(DLQ).**
13. **"단순 CRUD"라는 이유로 아키텍처를 우회하는 것ì다.**

---

## 부록 A. 실용주의 헌법 대비 차이점 요약

| 항목 | 실용주의 (Pragmatic) | 순수주의 (Purist) |
|---|---|---|
| **모듈 구조** | 단일 모듈 허용, 멀티 모듈 권장 | 멀티 모듈 강제, 단일 모듈 금지 |
| **Input Port** | 선택적 사용 ("필요할 때만") | 모든 유스케이스에 필수 |
| **Presentation DTO → Application** | 직접 전달 허용 | 명시적 매핑 강제, 직접 전달 금지 |
| **Domain 의존성** | `jakarta.validation 허용 | 어떠한 외부 의존성도 금지 |
| **단순 CRUD 예외** | Read-only 조회, 마스터 코드 등 우회 허용 | 예외 없음, 모든 접근이 Port를 경유 |
| **Inbound Adapter → Domain** | 직접 참조 가능 (application을 통해 간접 포함) | Domain 직접 참조 금지, Application Port에만 의존 |
| **계층 간 매핑** | 불필요한 매핑 생략 허용 | 모든 경계에서 명시적 매핑 강제 |
| **Domain Model** | Rich/Anemic 혼용 가능 (암묵적) | Rich Ddel 강제, Anemic 금지 |
| **Value Object** | 선택적 사용 | 원시 타입 포장 강제 (Primitive Obsession 금지) |
| **ACL** | 언급만 | 외부 Context 연동 시 ACL 강제 |
| **Bean 등록** | @ComponentScan 허용 | 수동 @Bean 등록 권장, 명시적 조립 |
| **Configuration 모듈** | application-api에 통합 | 별도 configuration 모듈로 분리 |
| **Domain Service → Repository** | 암묵적 허용 | 금지 (Application Service가 데이터 전달) |
| **ArchUnit** | 1차 방ì  | 2차 방어선 (1차는 멀티 모듈) |
