---
paths: "**/application/**"
---

# Application Layer Rules

애플리케이션 계층 규칙 — `**/application/**` 경로에 자동 적용.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.

---

## 1. 의존성 제약 (A-1)

- MUST NOT: Application 모듈에서 `domain` 이외의 모듈/라이브러리에 의존한다.
- MUST NOT: `build.gradle.kts`에 Spring, jOOQ, Jackson, jakarta.validation, `spring-tx` 등 기술 의존성을 포함한다.
- MUST: `build.gradle.kts`에 `implementation(project(":boilerplate-{bc}-domain"))`만 허용.
- MUST: Application Service 소스 파일에 Spring `import`가 단 하나도 없어야 한다.

> 근거: ADR-0002

---

## 2. Input Port 필수 (A-2)

- MUST: 모든 UseCase(Command/Query 포함)는 Input Port 인터페이스로 정의한다.
- MUST NOT: "단순 CRUD라서 생략"한다. 예외 없음.
- MUST: Input Port 인터페이스는 `application/port/input/` 패키지에 위치한다.
- MUST: 구현체는 `{Verb}{Subject}Service`로 네이밍한다 (예: `RegisterUserService`).

> 근거: ADR-0009

```java
// Input Port 인터페이스 — 반드시 정의
public interface RegisterUserUseCase {
    UserResult execute(RegisterUserCommand command);
}

// Input Port 인터페이스 — 단순 조회도 예외 없음
public interface FindUserByIdUseCase {
    UserSummary execute(FindUserByIdQuery query);
}

// Application Service — Input Port 구현, 순수 Java 클래스 (Spring 어노테이션 없음)
public class RegisterUserService implements RegisterUserUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final Clock clock;

    public RegisterUserService(LoadUserPort loadUserPort,
                               SaveUserPort saveUserPort,
                               Clock clock) {
        this.loadUserPort = loadUserPort;
        this.saveUserPort = saveUserPort;
        this.clock = clock;
    }

    @Override
    public UserResult execute(RegisterUserCommand command) {
        var now = clock.instant();

        // 1. Command → Domain VO 변환
        var name = new UserName(command.name());
        var ownerId = new OwnerId(command.ownerId());

        // 2. 비즈니스 규칙 검증 (LoadPort 사용)
        if (loadUserPort.existsByName(name)) {
            throw new UserAlreadyExistsException(name.value());
        }

        // 3. 도메인 행위 위임
        var user = User.create(name, ownerId, now);

        // 4. 저장 (이벤트 수거·Outbox 기록은 Adapter가 수행)
        saveUserPort.save(user);

        // 5. 결과 변환
        return new UserResult(user.id().value().toString(), user.name().value());
    }
}
```

---

## 3. 트랜잭션 (A-4, T-1)

### A-4 @Transactional 금지

- MUST NOT: Application Service 클래스에 `@Transactional` 또는 트랜잭션 관련 어노테이션을 직접 부착한다.
- MUST NOT: Application 모듈의 `build.gradle.kts`에 `spring-tx` 의존성을 포함한다.
- MUST: 트랜잭션 경계는 `configuration` 모듈에서 프록시(AOP 또는 `TransactionProxyFactoryBean`)로 외부에서 적용한다.

> 근거: ADR-0008

### T-1 UseCase = TX 경계

- MUST: UseCase(Input Port 구현체)가 트랜잭션의 경계이다.
- Command UseCase: R/W TX
- Query UseCase: R/O TX

```java
// configuration 모듈에서 TX 프록시 적용 예시
@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            LoadUserPort loadPort,
            SaveUserPort savePort,
            Clock clock,
            PlatformTransactionManager txManager) {

        // 순수 Java 인스턴스 생성 (어노테이션 없음)
        var service = new RegisterUserService(loadPort, savePort, clock);

        // 트랜잭션 프록시를 감싸서 반환
        var proxyFactory = new TransactionProxyFactoryBean();
        proxyFactory.setTarget(service);
        proxyFactory.setTransactionManager(txManager);

        var txAttr = new Properties();
        txAttr.setProperty("execute*", "PROPAGATION_REQUIRED");
        proxyFactory.setTransactionAttributes(txAttr);
        proxyFactory.afterPropertiesSet();

        return (RegisterUserUseCase) proxyFactory.getObject();
    }
}
```

---

## 4. 4가지 허용 역할 (A-5)

Application Service는 오케스트레이터. **비즈니스 판단 금지. 4가지 역할만 수행한다.**

| 순서 | 역할 | 설명 |
|------|------|------|
| 1 | **입력 변환** | Command 원시 타입 → Domain VO 생성 |
| 2 | **조회** | Output Port(LoadPort)로 Aggregate 로드 |
| 3 | **위임** | 도메인 객체의 행위 메서드 호출 |
| 4 | **저장** | `savePort.save(aggregate)` |

- MUST NOT: Application Service에 비즈니스 정책 판단(if/else 비즈니스 분기)을 둔다.
- MUST NOT: EventPublisher를 직접 호출한다. 이벤트 수거·발행은 Adapter(SavePort 구현체) 책임.
- MUST NOT: 기술 키워드(`DSLContext`, `WebClient`, `HttpServletRequest`, `@Transactional` 등)를 사용한다.
- MUST NOT: Presentation DTO(Web Request/Response)를 직접 참조한다.

```java
// ❌ BAD — Application Service에 비즈니스 판단
public class SuspendUserService implements SuspendUserUseCase {
    public void execute(SuspendUserCommand cmd) {
        var user = loadPort.findById(new UserId(cmd.userId())).orElseThrow();
        if (user.status() != UserStatus.ACTIVE) {   // ← 비즈니스 판단 금지
            throw new UserNotActiveException(user.id());
        }
        user.setStatus(UserStatus.SUSPENDED);        // ← Setter 금지
        savePort.save(user);
    }
}

// ✅ GOOD — Application은 4가지 역할만
public class SuspendUserService implements SuspendUserUseCase {
    public void execute(SuspendUserCommand cmd) {
        // 1. 변환
        var userId = new UserId(UUID.fromString(cmd.userId()));
        // 2. 조회
        var user = loadPort.findById(userId).orElseThrow();
        // 3. 위임 (비즈니스 로직은 Entity 내부)
        user.suspend(cmd.reason(), clock.instant());
        // 4. 저장
        savePort.save(user);
    }
}
```

---

## 5. UseCase 격리 (A-8)

- MUST NOT: UseCase에서 다른 UseCase를 직접 호출한다.
- MUST: 여러 UseCase가 협력해야 하는 경우, 이벤트 기반 분리를 사용한다 (A-9 참조 → cqrs.md).
- 근거: UseCase 간 직접 호출은 트랜잭션 중첩과 순환 의존을 초래한다.

> 근거: ADR-0010

---

## 6. DTO Record 강제 (A-10)

- MUST: Command, Query, Result 등 모든 Application DTO는 `record`로 정의한다.
- MUST NOT: Application DTO에 Setter를 둔다.
- MUST: Command/Query는 Compact Constructor에서 구조적 유효성(null 체크, 형식)을 검증한다.

```java
// Command — 원시 타입 필드, self-validation
public record RegisterUserCommand(String name, String ownerId) {
    public RegisterUserCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("ownerId must not be blank");
        }
    }
}

// Query — 원시 타입 필드, self-validation
public record FindUserByIdQuery(String userId) {
    public FindUserByIdQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}

// Result — 불변 record
public record UserResult(String id, String name) {}
```

> **Command/Query 필드 타입 규칙**: Command와 Query의 필드는 원시 타입(String, Long, UUID 등)으로 정의한다. Domain VO로의 변환은 UseCase(Application Service)가 수행한다. Domain Value Object를 Command/Query 필드 타입으로 사용하지 않는다. (CQRS 규칙 A-7 — 상세는 `cqrs.md` 참조)

---

## Output Port 위치 (A-3)

Output Port(LoadPort, SavePort, QueryPort)는 Application 계층의 `port/output/` 패키지에 정의한다. Output Port 3분할 원칙 및 CQRS 상세는 **`cqrs.md`**를 참조한다.

```
application/port/
    input/
        RegisterUserUseCase.java    # Command UseCase
        FindUserByIdUseCase.java    # Query UseCase
    output/
        LoadUserPort.java           # 도메인 객체 로드 (Optional 반환 강제)
        SaveUserPort.java           # 저장 + 이벤트 수거
        UserQueryPort.java          # DTO 직접 반환 (Aggregate 비경유)
```

---

## fallback 지시문

---
> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> 명시된 ADR 번호에 해당하는 `docs/decisions/` 파일을 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
