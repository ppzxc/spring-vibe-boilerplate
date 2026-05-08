# Phase 4 — 보안/관측성 실행 계획

## Context

Phase 3까지 Identity BC 전체 스택 구현 완료(domain→E2E, 5게이트 통과). 그러나 현 상태는 **모든 `/api/**` 엔드포인트가 인증 없이 노출**되며, Filter에서 JWT를 파싱해 `RequestContext`로 바인딩하는 흐름도 부재. 또한 OpenTelemetry 카탈로그 항목은 등록되어 있으나 boot-api에 미배선이며, ECS 로깅은 Spring Boot 4 내장 기능으로 부분 활성화된 상태.

본 Phase는 두 트랙을 병행해 인프라 골격을 마무리한다. 두 트랙은 사용자 결정에 따라 **JWT는 Spring Security + jjwt(Resource Server) 패턴**을 채택한다.

| 트랙 | 목적 | 산출 |
|------|------|------|
| 4A — 보안 | 인증·인가 미들웨어 + ScopedValue 컨텍스트 전파 | JwtAuthenticationFilter, RequestContext, ScopedValueAuthorizationPolicy, SecurityConfiguration |
| 4B — 관측성 | 세 기둥(Logs/Traces/Metrics) 완전 배선 + Health Probe | spring-boot-starter-opentelemetry + Health Indicator + spring-boot-docker-compose |

## Goals (Phase 4 완료 기준)

1. 인증된 요청에서 `RequestScope.CTX.get()`로 `userId/tenantId/permissions` 접근 가능.
2. 권한 부족 시 403 + `application/problem+json`(`code: PERMISSION_DENIED`) 반환.
3. JWT 미첨부/만료/서명 위배 시 401 반환.
4. `/actuator/health/{liveness,readiness}` 활성, DatabaseHealthIndicator 동작.
5. 로컬 OTel collector(LGTM) 부팅 후 traceId가 ECS JSON 로그에 자동 주입.
6. 기존 E2E 3케이스가 보안 활성화 상태에서도 통과(회원가입은 `permitAll()`로 인증 우회).
7. `./gradlew check` 5게이트 + 신규 보안/관측성 테스트 모두 BUILD SUCCESSFUL.

## Non-Goals

- IDP의 토큰 발급 엔드포인트(`/oauth2/token`) 구현. → 별도 Phase.
- Refresh token rotation, Token Revocation List(security.md §9). → 별도 Phase.
- Permission DB 모델(Role/Permission Aggregate). → Phase 6+ (Identity 보강).
- 멀티테넌시. → 본 Phase는 `tid` claim을 단순히 ScopedValue에 보관만. WHERE tenant_id 필터링은 미구현.

---

## 4A. 보안 트랙

### 4A.1 의존성 추가

`gradle/libs.versions.toml`:

```toml
[versions]
io-jsonwebtoken-jjwt = "0.12.6"

[libraries]
org-springframework-boot-starter-oauth2-resource-server = { module = "org.springframework.boot:spring-boot-starter-oauth2-resource-server", version.ref = "org-springframework-boot" }
io-jsonwebtoken-jjwt-api = { module = "io.jsonwebtoken:jjwt-api", version.ref = "io-jsonwebtoken-jjwt" }
io-jsonwebtoken-jjwt-impl = { module = "io.jsonwebtoken:jjwt-impl", version.ref = "io-jsonwebtoken-jjwt" }
io-jsonwebtoken-jjwt-jackson = { module = "io.jsonwebtoken:jjwt-jackson", version.ref = "io-jsonwebtoken-jjwt" }
```

> jjwt는 IDP 측 토큰 발급(Phase 6+)에서 주로 사용. Resource Server 검증은 Spring Security oauth2-resource-server가 nimbus-jose-jwt를 자동 포함하므로 별도 라이브러리 불필요. 본 Phase에서 jjwt를 등록하는 이유는 Phase 6 IDP 발급기 추가 시 카탈로그 항목 선행 준비.

`boilerplate-boot-api/build.gradle.kts` dependencies:

```kotlin
implementation(libs.org.springframework.boot.starter.security)
implementation(libs.org.springframework.boot.starter.oauth2.resource.server)
implementation(libs.io.jsonwebtoken.jjwt.api)
runtimeOnly(libs.io.jsonwebtoken.jjwt.impl)
runtimeOnly(libs.io.jsonwebtoken.jjwt.jackson)
testImplementation(libs.org.springframework.security.spring.security.test)
```

### 4A.2 RequestContext + RequestScope (ScopedValue) 위치 결정

| 후보 | 장점 | 단점 |
|------|------|------|
| **A. `boilerplate-shared-security` 신규 모듈** | BC 간 공유 가능 | 모듈 추가 비용 |
| **B. `boilerplate-boot-api` 내부** | 즉시 가능 | 다른 BC adapter 모듈에서 import 불가 |
| **C. 각 BC `adapter-input-api`에 복제** | 분리 | DRY 위반, ArchUnit 검증 어려움 |

**선택: A — `boilerplate-shared-security` 모듈 신설**. shared-event와 동일 패턴으로, BC 간 공유되는 인프라 contract만 보관. ScopedValue 인스턴스는 BC들이 모두 같은 객체를 참조해야 격리가 의미 있음.

```
boilerplate/boilerplate-shared-security/
  build.gradle.kts                          # label("java"), no Spring deps
  src/main/java/io/github/ppzxc/boilerplate/shared/security/
    RequestContext.java                     # record(userId, tenantId, permissions)
    RequestScope.java                       # public static final ScopedValue<RequestContext> CTX
    Permission.java                         # record(value) + resource:scope 검증
    AuthorizationPolicy.java               # interface
    ScopedValueAuthorizationPolicy.java    # 구현체
    AccessDeniedException.java             # 순수 Java 예외 (Spring 의존 없음)
```

`RequestContext`:

```java
public record RequestContext(
    UUID userId,
    String tenantId,
    Set<Permission> permissions
) {
  public RequestContext {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(tenantId, "tenantId");
    permissions = Set.copyOf(Objects.requireNonNull(permissions, "permissions"));
  }
}
```

`RequestScope`:

```java
public final class RequestScope {
  public static final ScopedValue<RequestContext> CTX = ScopedValue.newInstance();
  private RequestScope() {}
}
```

`Permission` (security.md §4 형식 검증):

```java
public record Permission(String value) {
  public Permission {
    Objects.requireNonNull(value);
    if (!value.matches("[a-z]+:[a-z]+")) {
      throw new IllegalArgumentException("Permission must be resource:scope: " + value);
    }
  }
}
```

`AccessDeniedException` (Spring 의존 없음):

```java
public final class AccessDeniedException extends RuntimeException {
  private final String requiredPermission;
  public AccessDeniedException(String requiredPermission) {
    super("Required permission: " + requiredPermission);
    this.requiredPermission = requiredPermission;
  }
  public String requiredPermission() { return requiredPermission; }
}
```

> 본 모듈은 외부 의존 0. Spring/Jakarta import 금지. ArchUnit으로 강제(§4A.7).

### 4A.3 SecurityConfiguration (Resource Server)

위치: `boilerplate/boilerplate-boot-api/src/main/java/.../boot/security/SecurityConfiguration.java`

```java
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtRequestContextFilter ctxFilter) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/identity/users").permitAll()  // 회원가입 익명 허용
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAfter(ctxFilter, BearerTokenAuthenticationFilter.class)
        .exceptionHandling(eh -> eh
            .authenticationEntryPoint(new ProblemDetailAuthEntryPoint())
            .accessDeniedHandler(new ProblemDetailAccessDeniedHandler()))
        .build();
  }

  @Bean
  JwtDecoder jwtDecoder(@Value("${security.jwt.jwk-set-uri:}") String jwkSetUri,
                        @Value("${security.jwt.hmac-secret:}") String hmacSecret) {
    if (!jwkSetUri.isBlank()) {
      return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
    var key = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }
}
```

`application.yml` 신규 키:

```yaml
security:
  jwt:
    jwk-set-uri: ${JWT_JWK_SET_URI:}
    hmac-secret: ${JWT_HMAC_SECRET:dev-secret-change-me-in-production-min-256-bits}
```

> 운영에선 jwk-set-uri 사용. 개발/테스트는 HMAC.

**E2E 호환성**: `POST /api/identity/users`는 `permitAll()`이므로 Phase 3 E2E 3케이스(회원가입 201, 중복 409, Api-Version 누락 400)가 토큰 없이 그대로 통과.

### 4A.4 JwtRequestContextFilter

위치: `boilerplate/boilerplate-boot-api/src/main/java/.../boot/security/JwtRequestContextFilter.java`

`OncePerRequestFilter` 상속. `BearerTokenAuthenticationFilter` 이후에 동작 → `Authentication`에 이미 `Jwt` principal이 있는 상태.

```java
@Component
class JwtRequestContextFilter extends OncePerRequestFilter {

  private static final String TID_CLAIM = "tid";
  private static final String SCOPE_CLAIM = "scope";

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      var jwt = jwtAuth.getToken();
      var ctx = new RequestContext(
          UUID.fromString(jwt.getSubject()),
          jwt.getClaimAsString(TID_CLAIM) == null ? "default" : jwt.getClaimAsString(TID_CLAIM),
          parsePermissions(jwt.getClaimAsString(SCOPE_CLAIM)));
      try {
        ScopedValue.where(RequestScope.CTX, ctx)
            .call(() -> { chain.doFilter(req, res); return null; });
      } catch (RuntimeException | IOException | ServletException e) {
        throw e;
      } catch (Exception e) {
        throw new ServletException(e);
      }
    } else {
      chain.doFilter(req, res);   // 비인증 경로(permitAll)
    }
  }

  private static Set<Permission> parsePermissions(String scope) {
    if (scope == null || scope.isBlank()) return Set.of();
    return Arrays.stream(scope.split(" "))
        .filter(s -> s.matches("[a-z]+:[a-z]+"))
        .map(Permission::new)
        .collect(Collectors.toUnmodifiableSet());
  }
}
```

> security.md §3: ScopedValue 바인딩 위치. observability.md §6: ThreadLocal 금지, ScopedValue 필수.

### 4A.5 AuthorizationPolicy Output Port + 구현체

`boilerplate-shared-security` 모듈에 인터페이스 + 구현체를 함께 둔다(순수 Java). Identity BC는 Domain에서 직접 인가하므로 본 Phase에서 미사용. 후속 BC(Notification 등)가 Application Output Port로 사용.

```java
// boilerplate-shared-security
public interface AuthorizationPolicy {
  void requirePermission(String resourceScope);  // "user:deactivate" 형식
}

public final class ScopedValueAuthorizationPolicy implements AuthorizationPolicy {
  @Override
  public void requirePermission(String resourceScope) {
    var perm = new Permission(resourceScope);
    var ctx = RequestScope.CTX.orElse(null);
    if (ctx == null || !ctx.permissions().contains(perm)) {
      throw new AccessDeniedException(resourceScope);
    }
  }
}
```

### 4A.6 ProblemDetail 핸들러 (401/403)

위치: `boilerplate/boilerplate-boot-api/src/main/java/.../boot/security/`

```java
class ProblemDetailAuthEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
      throws IOException {
    write(res, 401, "AUTHENTICATION_REQUIRED", "Bearer token is required", req.getRequestURI());
  }
}

class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res,
      org.springframework.security.access.AccessDeniedException ex) throws IOException {
    write(res, 403, "PERMISSION_DENIED", ex.getMessage(), req.getRequestURI());
  }
}

private static void write(HttpServletResponse res, int status, String code,
    String detail, String instance) throws IOException {
  res.setStatus(status);
  res.setContentType("application/problem+json");
  var body = ("""
      {"type":"about:blank","title":"%s","status":%d,\
      "code":"%s","detail":"%s","instance":"%s"}""")
      .formatted(HttpStatus.valueOf(status).getReasonPhrase(), status, code, detail, instance)
      .getBytes(StandardCharsets.UTF_8);
  res.setContentLength(body.length);
  res.getOutputStream().write(body);  // charset 자동 추가 방지 (Phase 3.7 학습)
}
```

Identity의 `ProblemDetailExceptionHandler`에 자체 `AccessDeniedException` 핸들러 추가:

```java
@ExceptionHandler(io.github.ppzxc.boilerplate.shared.security.AccessDeniedException.class)
ResponseEntity<ProblemDetail> handle(AccessDeniedException ex) {
  var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
  problem.setTitle("Permission Denied");
  problem.setProperty("code", "PERMISSION_DENIED");
  problem.setProperty("requiredPermission", ex.requiredPermission());
  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
}
```

### 4A.7 ArchitectureTest 보강

`boilerplate-boot-api/src/test/java/.../architecture/ArchitectureTest.java`에 3개 규칙 추가:

```java
@Test
void shared_security_Spring_의존_금지() {
  noClasses().that().resideInAPackage("..shared.security..")
      .should().dependOnClassesThat().resideInAPackage("org.springframework..")
      .check(classes);
}

@Test
void domain_RequestScope_접근_금지() {
  noClasses().that().resideInAPackage("..domain..")
      .should().dependOnClassesThat()
      .haveFullyQualifiedName("io.github.ppzxc.boilerplate.shared.security.RequestScope")
      .check(classes);
}

@Test
void domain_SecurityContextHolder_접근_금지() {
  noClasses().that().resideInAPackage("..domain..")
      .should().dependOnClassesThat()
      .resideInAPackage("org.springframework.security..")
      .check(classes);
}
```

### 4A.8 테스트

| 테스트 파일 | 기법 | 케이스 |
|-----------|------|--------|
| `JwtRequestContextFilterTest` | `MockMvc` + `with(jwt())` | 정상 JWT → ScopedValue 바인딩 / sub UUID 변환 / scope → Permission Set |
| `ScopedValueAuthorizationPolicyTest` | 순수 JUnit + `ScopedValue.where().run()` | permission 매칭 / 미매칭 → AccessDeniedException / CTX 미바인딩 → AccessDeniedException |
| `SecurityFilterChainE2ETest` | `@SpringBootTest` + `RestClient` | 토큰 미첨부 GET → 401 / 정상 토큰 GET → 200 / 만료 토큰 → 401 |
| `UserRegistrationE2ETest` (수정 불필요) | 기존 그대로 | `POST /api/identity/users`는 `permitAll()`이므로 토큰 없이 통과 |

---

## 4B. 관측성 트랙

### 4B.1 의존성 추가

`gradle/libs.versions.toml`에 추가:

```toml
[libraries]
org-springframework-boot-starter-opentelemetry = { module = "org.springframework.boot:spring-boot-starter-opentelemetry", version.ref = "org-springframework-boot" }
org-springframework-boot-docker-compose = { module = "org.springframework.boot:spring-boot-docker-compose", version.ref = "org-springframework-boot" }
```

`boilerplate-boot-api/build.gradle.kts`:

```kotlin
implementation(libs.org.springframework.boot.starter.opentelemetry)
developmentOnly(libs.org.springframework.boot.docker.compose)
```

### 4B.2 application.yml 보강

기존 OTLP 설정은 유지하고 다음 항목 추가:

```yaml
spring:
  docker:
    compose:
      enabled: true
      lifecycle-management: start-and-stop
      file: ./docker-compose.yml

management:
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING:1.0}   # 운영 0.1 권장
  observations:
    annotations:
      enabled: true   # @Observed 활성화

security:
  jwt:
    jwk-set-uri: ${JWT_JWK_SET_URI:}
    hmac-secret: ${JWT_HMAC_SECRET:dev-secret-change-me-in-production-min-256-bits}
```

`application-test.yml` 신규 (Testcontainers 환경):

```yaml
spring:
  docker:
    compose:
      enabled: false
management:
  tracing:
    sampling:
      probability: 0.0
```

### 4B.3 docker-compose.yml LGTM 보강

기존 PostgreSQL에 Grafana LGTM stack 추가:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: boilerplate
      POSTGRES_USER: boilerplate
      POSTGRES_PASSWORD: boilerplate
    ports: ["5432:5432"]

  lgtm:
    image: grafana/otel-lgtm:latest
    ports:
      - "3000:3000"   # Grafana UI
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
```

### 4B.4 DatabaseHealthIndicator

위치: `boilerplate/boilerplate-boot-api/src/main/java/.../boot/observability/DatabaseHealthIndicator.java`

```java
@Component
class DatabaseHealthIndicator implements HealthIndicator {
  private final DSLContext dsl;

  DatabaseHealthIndicator(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public Health health() {
    try {
      dsl.selectOne().fetch();
      return Health.up().withDetail("dialect", dsl.dialect().name()).build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
```

### 4B.5 ECS 로깅 검증

이미 `logging.structured.format.console: ecs` 활성. traceId/spanId가 ECS JSON에 자동 주입되는지 통합 테스트로 검증.

`LogTraceCorrelationTest`: 회원가입 1건 호출 → `OutputCaptureExtension`으로 stdout 캡처 → JSON 파싱 → `trace.id` 필드 존재 검증.

### 4B.6 테스트

| 테스트 파일 | 검증 내용 |
|-----------|---------|
| `DatabaseHealthIndicatorTest` | `@SpringBootTest` + Testcontainers — `Health.up()` |
| `ActuatorProbeTest` | `/actuator/health/liveness` 200, `/actuator/health/readiness` 200 |
| `LogTraceCorrelationTest` | stdout 캡처 → JSON line에 `trace.id` 필드 존재 |

---

## Phase 4 실행 순서

1. **4A.1** 의존성 추가 (libs.versions.toml + boot-api build.gradle.kts).
2. **4A.2** `boilerplate-shared-security` 모듈 신설, settings.gradle.kts 등록.
3. **4A.2** `RequestContext`, `RequestScope`, `Permission`, `AuthorizationPolicy`, `ScopedValueAuthorizationPolicy`, `AccessDeniedException` 작성.
4. **4A.7** ArchitectureTest 3종 추가 → 컴파일 확인.
5. **4A.4** `JwtRequestContextFilter` 작성 + 단위 테스트.
6. **4A.6** `ProblemDetailAuthEntryPoint`, `ProblemDetailAccessDeniedHandler` 작성.
7. **4A.3** `SecurityConfiguration` 작성, `application.yml` security 키 추가.
8. Identity `ProblemDetailExceptionHandler`에 `AccessDeniedException` 핸들러 추가.
9. **4A.8** `SecurityFilterChainE2ETest` 작성 → 기존 E2E 통과 확인.
10. **4B.1** OpenTelemetry / docker-compose support 의존성 추가.
11. **4B.2** `application.yml` 보강 + `application-test.yml` 신규 작성.
12. **4B.3** `docker-compose.yml`에 LGTM 추가.
13. **4B.4** `DatabaseHealthIndicator` 작성.
14. **4B.6** Actuator probe + log trace correlation 테스트.
15. `./gradlew check --no-daemon` 5게이트 통과 확인.

## Phase 4 변경 파일 매트릭스

**신규 파일**:
- `boilerplate/boilerplate-shared-security/build.gradle.kts`
- `boilerplate/boilerplate-shared-security/src/main/java/.../shared/security/{RequestContext,RequestScope,Permission,AuthorizationPolicy,ScopedValueAuthorizationPolicy,AccessDeniedException}.java`
- `boilerplate/boilerplate-boot-api/src/main/java/.../boot/security/{SecurityConfiguration,JwtRequestContextFilter,ProblemDetailAuthEntryPoint,ProblemDetailAccessDeniedHandler}.java`
- `boilerplate/boilerplate-boot-api/src/main/java/.../boot/observability/DatabaseHealthIndicator.java`
- `boilerplate/boilerplate-boot-api/src/main/resources/application-test.yml`
- 테스트 파일 4종 (JwtRequestContextFilterTest, ScopedValueAuthorizationPolicyTest, SecurityFilterChainE2ETest, ActuatorProbeTest/LogTraceCorrelationTest/DatabaseHealthIndicatorTest).

**수정 파일**:
- `gradle/libs.versions.toml` (jjwt 3종 + opentelemetry + docker-compose)
- `settings.gradle.kts` (shared-security 모듈 등록)
- `boilerplate/boilerplate-boot-api/build.gradle.kts` (보안/관측성 의존성 추가)
- `boilerplate/boilerplate-boot-api/src/main/resources/application.yml` (security + docker.compose 키)
- `docker-compose.yml` (lgtm 서비스 추가)
- `boilerplate/boilerplate-boot-api/src/test/java/.../architecture/ArchitectureTest.java` (3 룰 추가)
- `boilerplate/identity/boilerplate-identity-adapter-input-api/src/main/java/.../api/exception/ProblemDetailExceptionHandler.java` (AccessDeniedException 핸들러 추가)

## Phase 4 검증

```bash
# 5게이트
./gradlew check --no-daemon

# 부팅 + 보안 흐름 검증
docker compose up -d
./gradlew :boilerplate-boot-api:bootRun

# 401: 토큰 없는 인증 필요 경로
curl -i localhost:8080/api/identity/users/00000000-0000-0000-0000-000000000000
# 기대: 401 + Content-Type: application/problem+json + code: AUTHENTICATION_REQUIRED

# 201: 회원가입은 permitAll
curl -i -X POST localhost:8080/api/identity/users \
  -H "Content-Type: application/json" -H "Api-Version: 2026-05-08" \
  -d '{"userName":"홍","email":"sec@test.com","password":"hashed"}'
# 기대: 201

# health
curl -s localhost:8080/actuator/health/readiness
# 기대: {"status":"UP"}
curl -s localhost:8080/actuator/health/liveness
# 기대: {"status":"UP"}

# trace 검증: Grafana http://localhost:3000 → Tempo → service.name=boilerplate
```
