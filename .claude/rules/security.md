---
description: BC 기반 인가, Permission 모델 (resource:scope), JWT 클레임 매핑, ScopedValue 보안 컨텍스트
alwaysApply: true
---

# Security & Authorization Rules

인가 규칙 — 항상 로드.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.

---

## 1. BC 기반 인가 모델

인가 처리 위치는 BC의 성격에 따라 결정된다.

| BC | 인가 위치 | 이유 |
|----|----------|------|
| **Identity BC** | Domain 계층 | User, Role, Permission이 비즈니스 도메인 그 자체 |
| **다른 BC** | Application 계층 (Output Port) | 인가 정책 소비만 함 |

- MUST NOT: 다른 BC의 Domain 계층에 인가 개념(Permission 체크, role 조회)을 포함한다.

---

## 2. Requester(요청자)와 Target(대상) 구분

| 구분 | Requester | Target |
|------|-----------|--------|
| 질문 | "누가 요청했는가" | "누구에 대한 작업인가" |
| 위치 | ScopedValue (`RequestScope.CTX`) | Command 필드 |
| 계층 | Application이 인가 판단 | Domain이 비즈니스 처리 |

```java
// Command — Target만 (비즈니스 데이터)
public record DeactivateUserCommand(UUID targetUserId) {
    public DeactivateUserCommand {
        Objects.requireNonNull(targetUserId, "targetUserId must not be null");
    }
}

// Application — Requester(인가) + Target(비즈니스) 분리
public class DeactivateUserService implements DeactivateUserUseCase {
    private final AuthorizationPolicy policy;
    private final LoadUserPort loadPort;
    private final SaveUserPort savePort;
    private final Clock clock;

    public void execute(DeactivateUserCommand cmd) {
        // 1. 인가 검사 — Requester 기반
        policy.checkPermission("user:deactivate");
        // 2. Domain 위임 — Target 기반
        var user = loadPort.findById(new UserId(cmd.targetUserId())).orElseThrow();
        user.deactivate(clock.instant());
        savePort.save(user);
    }
}

// Domain — Requester를 모름. Target에 대한 비즈니스만.
public void deactivate(Instant now) {
    if (this.status == UserStatus.DEACTIVATED) {
        throw UserException.alreadyDeactivated(this.id);
    }
    this.status = UserStatus.DEACTIVATED;
    registerEvent(new UserDeactivatedEvent(
        UUIDv7.generate(), "UserDeactivatedEvent",
        this.id.value(), now, this.version));
}
```

**소유권 검증** (자기 리소스만 수정 가능한 경우):

```java
public void execute(UpdateProfileCommand cmd) {
    var requesterId = RequestScope.CTX.get().userId();
    if (!requesterId.value().equals(cmd.targetUserId())) {
        throw new AccessDeniedException("자기 프로필만 수정 가능");
    }
    // 이하 비즈니스 로직
}
```

### 2단계 인가 판단 (Resource Server)

IDP가 아닌 다른 BC(또는 외부 서비스)가 Resource Server로 동작할 때 2단계 인가를 수행한다.

```
1단계 (Scope):     "이 앱의 토큰에 write scope가 있는가?" → Filter/Interceptor (Adapter)
2단계 (Permission): "이 사용자가 user:create 권한이 있는가?" → Application 계층 (AuthorizationPolicy)
```

- MUST: 1단계 Scope 검사는 Adapter(Filter/Interceptor)에서 수행한다.
- MUST: 2단계 Permission 검사는 Application 계층에서 AuthorizationPolicy Output Port를 통해 수행한다.
- MUST NOT: Controller에서 직접 Permission 검사를 수행한다.

---

## 3. 계층별 RequestContext 접근 규칙

| 계층 | 접근 유형 | 방법 | 규칙 |
|------|----------|------|------|
| **Filter (Adapter In)** | 바인딩 | `ScopedValue.where(CTX, ctx).run(...)` | MUST |
| **Controller** | 사용 금지 | Command에 비즈니스 데이터만 | MUST NOT |
| **Application Service** | 읽기 (필요 시) | `RequestScope.CTX.get()` | MAY |
| **Domain** | **접근 불가** | ScopedValue import 금지 | **MUST NOT** |
| **Persistence Adapter (Out)** | 읽기 (필요 시) | `RequestScope.CTX.get()` — 메시지 헤더 주입 | MAY |

- MUST NOT: Domain 계층에서 `RequestScope.CTX`, `SecurityContextHolder`, `HttpServletRequest` 등 요청 컨텍스트에 접근한다.

---

## 4. Permission 모델 (resource:scope, ADR-0013)

> 근거: ADR-0013

### 패턴: `{resource}:{scope}`

```
resource = 도메인 리소스 타입 (소문자, 단수형)
scope    = 행위 (소문자, 동사형)
```

**예시**:

| Resource | 허용 Scope |
|----------|-----------|
| `user` | `view`, `create`, `update`, `delete`, `deactivate` |
| `role` | `view`, `create`, `update`, `delete`, `assign` |
| `permission` | `view`, `create`, `grant`, `revoke` |
| `client` | `view`, `create`, `update`, `delete`, `register` |
| `token` | `issue`, `revoke`, `introspect` |

### Permission Value Object

```java
// Domain (Identity BC)
public record Permission(String value) {
    public Permission {
        Objects.requireNonNull(value);
        if (!value.matches("[a-z]+:[a-z]+")) {
            throw new IllegalArgumentException(
                "Permission must be resource:scope format: " + value);
        }
    }

    public String resource() { return value.split(":")[0]; }
    public String scope()    { return value.split(":")[1]; }
}
```

### AuthorizationPolicy (다른 BC용 Application Output Port)

```java
// Application 계층 Output Port
public interface AuthorizationPolicy {
    void checkPermission(String action);  // "user:deactivate" 형식
}

// Adapter 구현 — ScopedValue에서 permissions 읽기
@Component
public class ScopedValueAuthorizationPolicy implements AuthorizationPolicy {
    @Override
    public void checkPermission(String action) {
        var permissions = RequestScope.CTX.get().permissions();
        if (permissions.stream().noneMatch(p -> p.value().equals(action))) {
            throw new AccessDeniedException(action);
        }
    }
}
```

---

## 5. OAuth2 Scope vs Permission 구분

| 계층 | 소비자 | 예시 | 역할 |
|------|--------|------|------|
| **OAuth2 Scope** | 클라이언트 앱 (외부) | `read`, `write`, `admin` | Permission의 Named Group — 앱용 거친 제어 |
| **Permission** | 사용자 (사람) | `user:create`, `role:assign` | 최소 인가 단위 |

- MUST: OAuthScope는 Permission의 집합으로 정의한다.

```java
// Domain (Identity BC)
public record OAuthScope(String name, Set<Permission> permissions) {
    public OAuthScope {
        Objects.requireNonNull(name);
        permissions = Set.copyOf(permissions);
    }
}
```

---

## 6. Token Intersection (JWT scope × DB 권한 교집합)

**Authorization Code / Password (사용자 개입)**:
```
grantedPermissions = intersect(
    client.scopePermissions(),   // 이 앱에 허용된 권한
    user.rolePermissions()       // 이 사용자가 가진 권한
)
```

**Client Credentials (M2M, 사용자 없음)**:
```
grantedPermissions = client.scopePermissions()
```

```java
public class IssueTokenService implements IssueTokenUseCase {
    public TokenResult execute(IssueTokenCommand cmd) {
        var client = loadClientPort.load(new ClientId(cmd.clientId()));
        var clientPermissions = client.scopes().stream()
            .flatMap(s -> s.permissions().stream())
            .collect(Collectors.toSet());

        Set<Permission> granted;
        if (cmd.grantType() == GrantType.CLIENT_CREDENTIALS) {
            granted = clientPermissions;
        } else {
            var user = loadUserPort.findById(new UserId(cmd.userId())).orElseThrow();
            var userPermissions = user.roleIds().stream()
                .map(loadRolePort::load)
                .flatMap(role -> role.permissions().stream())
                .collect(Collectors.toSet());
            granted = new HashSet<>(userPermissions);
            granted.retainAll(clientPermissions);   // 교집합
        }
        return tokenService.issue(cmd.grantType(), client, granted, clock.instant());
    }
}
```

---

## 7. JWT Claim 매핑

| JWT Claim | Java 필드 | VO 타입 |
|-----------|----------|--------|
| `sub` | userId | `UserId` |
| `tid` | tenantId | `TenantId` |
| `scope` | oauthScope | `String` |
| `client_id` | clientId | `ClientId` |

```java
// Filter (Adapter In)
public class JwtRequestContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var token = extractAndVerifyJwt((HttpServletRequest) req);
        var ctx = new RequestContext(
            new UserId(UUID.fromString(token.getClaim("sub"))),
            new TenantId(token.getClaim("tid")),
            parsePermissions(token)        // scope → Set<Permission>
        );
        ScopedValue.where(RequestScope.CTX, ctx)
            .run(() -> chain.doFilter(req, res));
    }
}
```

**JWT 클레임 네이밍 원칙**: claim 이름은 짧게(`sub`, `tid`), Java 코드는 풀네임(`userId`, `tenantId`).

---

## 8. Identity BC Domain Model

```
User:       UserId, Email, Credential, Set<RoleId>
Role:       RoleId, RoleName, Set<Permission>
Client:     ClientId, ClientSecret, Set<OAuthScope>, Set<GrantType>
Permission: String value (resource:scope VO)
OAuthScope: String name + Set<Permission>
```

- MUST: Role Aggregate가 Permission의 `grantPermission()`, `revokePermission()` 행위를 소유한다.
- MUST: User Aggregate는 `Set<RoleId>`로 Role을 ID 참조한다 (D-9).

---

## 9. 확장 원칙

### 멀티 테넌시

- MUST NOT: Permission 문자열에 테넌트 ID를 포함한다 (`user:view:tenant-abc`).
- MUST: `TenantId`는 JWT `tid` claim에서 추출하여 `RequestContext`에 포함한다.
- MUST: DB 쿼리에서 테넌트 격리는 `WHERE tenant_id = ?` 조건으로 처리한다.

### Instance-Level 접근 제어

- MUST NOT: `resource:scope:instanceId` 패턴을 사용한다.
- MUST: 특정 리소스 인스턴스 접근 제어는 Domain의 소유권 검증(비즈니스 규칙)으로 처리한다.

### 토큰 비대화 방지 (Permission이 수백 개인 경우)

엔터프라이즈 환경에서 Permission이 수백 개에 달하면 JWT HTTP Header Size Limit(8KB)을 초과할 수 있다.

| 방안 | JWT 내용 | Permission 조회 | 적합한 상황 |
|------|---------|----------------|-----------|
| **A. Introspection** (기본) | scope만 | Resource Server가 `/introspect` 또는 Redis 캐시 | Permission 수가 많고, 실시간 권한 변경 필요 |
| **B. Opaque Token** | 토큰 자체에 정보 없음 | 매 요청마다 IDP에 조회 | 보안 최우선, 토큰 탈취 위험 최소화 |
| **C. Permission이 적은 경우** | scope + permissions 둘 다 | 불필요 | Permission 수십 개 이하, 단순한 서비스 |

- MUST: 본 프로젝트는 방안 A(Introspection)를 기본으로 한다.
- MAY: 보안 최우선 환경에서는 방안 B(Opaque Token)를 선택할 수 있다.

### 외부 Resource Server 권한 갱신 전략

외부 Resource Server는 다른 JVM이므로 Spring Modulith 이벤트가 닿지 않는다.

**기본: 짧은 Access Token TTL + Refresh 순환**

```
Access Token TTL: 5~15분
Refresh Token: 장기 유효

1. Resource Server가 Access Token 검증 → 만료됨
2. Client가 Refresh Token으로 새 Access Token 요청
3. IDP가 그 시점의 최신 Permission으로 새 토큰 발급
→ 최대 지연 = Access Token TTL
```

**긴급: Token Revocation (강제 로그아웃, 권한 박탈)**

- IDP가 해당 토큰을 Revocation List에 등록 → Resource Server가 /introspect 확인 → 즉시 차단.

**프로젝트 성장 단계별 전략**

| 단계 | 인프라 | 내부 BC 간 | 외부 Resource Server |
|------|--------|----------|-------------------|
| **초기** | Spring Modulith만 | Modulith 이벤트 (in-process) | 짧은 TTL + Revocation |
| **성장기** | + Redis | Modulith 이벤트 + Redis pub/sub | Redis 캐시 + 이벤트 무효화 |
| **확장기** | + Kafka | Modulith Externalization → Kafka | Kafka 구독 또는 OpenID CAEP |

- SHOULD: 초기에는 짧은 Access Token TTL + Revocation으로 시작한다. 코드 변경 없이 인프라만 교체된다.

---

## 10. 시크릿 관리

- MUST NOT: `application.yml` / `application.properties`에 DB 비밀번호, API 키, JWT 시크릿을 하드코딩한다.
- MUST: 시크릿은 환경 변수(`${DATABASE_PASSWORD}`) 또는 시크릿 매니저(Vault, AWS Secrets Manager 등)로 주입한다.
- MUST NOT: 시크릿이 포함된 파일(`.env`, `credentials.json`, `*.pem`, `*.key`)을 Git에 커밋한다.
- MUST: `.gitignore`에 시크릿 파일 패턴을 포함한다.

```yaml
# ✅ GOOD — 환경 변수 참조
spring:
  datasource:
    password: ${DATABASE_PASSWORD}

jwt:
  secret: ${JWT_SECRET}

# ❌ BAD — 하드코딩
spring:
  datasource:
    password: mySecretPassword123
```

---

## 11. 금지 패턴

```java
// MUST NOT: Domain에서 RequestContext 접근
public void deactivate() {
    var requesterId = RequestScope.CTX.get().userId();  // 금지
}

// MUST NOT: 다른 BC의 Domain에 인가 개념 포함
public class Notification {
    public void send() {
        checkPermission("notification:send");  // Domain에서 금지
    }
}

// MUST NOT: Instance-Level Permission 패턴
var p = new Permission("document:edit:doc-123");  // instanceId 포함 금지

// MUST NOT: 비구조적 비동기에서 ScopedValue 읽기
CompletableFuture.runAsync(() -> {
    var ctx = RequestScope.CTX.get();  // 바인딩 유실
});

// MUST NOT: Domain Event에 요청 컨텍스트 포함
public record UserDeactivatedEvent(
    UUID eventId, String eventType, UUID aggregateId,
    Instant occurredAt, long aggregateVersion,
    UUID deactivatedBy  // MUST NOT — 요청자 정보는 메시지 헤더로
) {}
```

---

## fallback 지시문

---
> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> `docs/decisions/ADR-0013-*.md` 파일을 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
