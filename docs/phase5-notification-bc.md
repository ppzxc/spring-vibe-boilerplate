# Phase 5 — Notification BC + Integration Event 실행 계획

## Context

Phase 3에서 Identity BC가 `UserRegisteredIntegrationEvent`를 `event_publication`에 발행하는 것까지 완료. 그러나 본 이벤트를 수신해 부수 효과(웰컴 메일 전송 등)를 일으킬 컨슈머가 부재하여, Modulith 하이브리드 전략의 BC 간 통신 패턴이 실증되지 않은 상태.

본 Phase는 **두 번째 BC인 Notification을 신설**하여:
1. Identity BC와의 BC 간 통신을 Integration Event로 구현(ACL 패턴).
2. `boilerplate-{bc}-{layer}` 5모듈 패턴 + `adapter-input-event` 모듈을 처음 도입.
3. `@ApplicationModuleListener` 기반 비동기 핸들러를 검증.

## Goals (Phase 5 완료 기준)

1. Identity 회원가입 시 Notification BC가 `UserRegisteredIntegrationEvent`를 수신해 `notifications` 테이블에 1건 적재.
2. Notification BC가 단독 기동(`@ApplicationModuleTest(STANDALONE)`) 가능.
3. `ApplicationModules.verify()` 통과 — Identity ↔ Notification 간 직접 의존 0건.
4. E2E: Identity API 호출 → Notification 테이블에 알림 record 적재(Awaitility 폴링).
5. `./gradlew check` 5게이트 + 신규 통합 테스트 모두 BUILD SUCCESSFUL.

## Non-Goals

- 실제 외부 알림 채널(이메일 SMTP, SMS) 연동. → 본 Phase는 DB 적재 + 로그 출력만.
- Notification 사용자 환경설정(opt-out, 채널 선택). → 별도 Phase.
- Notification 발신 재시도/Dead Letter Queue. → Modulith의 자동 republish로 충분(at-least-once).

---

## 5.0 전략적 설계 (strategic-design.md §1~7)

### 5.0.1 Subdomain 분류

- **Notification BC = Supporting Domain**.
- 근거: 알림은 IDP/Resource Server의 차별화 요소가 아니다(경쟁 우위 X). 그러나 사내 SaaS 통합을 위해 도메인 어휘(채널, 템플릿, 발송 상태)가 별도로 필요해 외부 SaaS로 완전 대체 불가.
- DDD 깊이: **Simplified DDD** — Aggregate + VO 최소, 비즈니스 규칙 단순(상태 전이 PENDING→SENT→FAILED).

### 5.0.2 Ubiquitous Language

| 용어(한국어) | 코드 식별자 | 정의 |
|------------|-----------|------|
| 알림 | `Notification` | 사용자에게 전달할 메시지 한 건 |
| 알림 ID | `NotificationId` | UUIDv7 식별자 VO |
| 알림 채널 | `NotificationChannel` | enum: EMAIL, SMS, PUSH (현 단계 EMAIL만) |
| 알림 상태 | `NotificationStatus` | enum: PENDING, SENT, FAILED |
| 수신자 ID | `RecipientUserId` | Identity의 UserId를 참조하는 외부 키. ACL 경계 |
| 알림 본문 | `NotificationContent` | record(subject, body) |

### 5.0.3 Context Map 갱신

```
[Phase 5 이후]

  ┌─────────────────────┐  Published Language    ┌──────────────────────┐
  │   Identity BC       │ ─────────────────────→ │  Notification BC     │
  │   (Core, Upstream)  │  UserRegistered         │  (Supporting)        │
  │                     │  IntegrationEvent       │  ACL 패턴 (Downstream)│
  └─────────────────────┘  (shared-event)         └──────────────────────┘
            │                                              │
            └─────────── shared-security (CTX) ────────────┘
```

`docs/architecture/context-map.md`에 본 다이어그램 추가. 관계 패턴 = **Customer/Supplier with ACL**(Notification이 Identity 이벤트 스키마 변경에 영향받으나 자체 도메인 모델로 번역).

### 5.0.4 ADR 결정

- `ADR-0022-notification-bc-as-supporting-domain.md` 신설(선택). 자명한 결정이나 향후 Generic(외부 SaaS 대체) 전환 시 근거가 될 수 있어 작성 권장.

---

## 5.1 모듈 신설 (5종)

scaffold.md §신규 BC 모듈 초기화 체크리스트 그대로.

```
boilerplate/notification/
  boilerplate-notification-domain/                    # label("java","coverage-gate")
  boilerplate-notification-application/               # label("java","coverage-gate")
  boilerplate-notification-adapter-input-event/       # label("java","spring") — 첫 event-input 모듈
  boilerplate-notification-adapter-output-persist/    # label("java","spring","jooq")
  boilerplate-notification-configuration/             # label("java","spring")
```

**수정 파일**:
- `settings.gradle.kts` — 5개 모듈 등록.
- `boilerplate/boilerplate-boot-api/build.gradle.kts` — `implementation(project(":boilerplate-notification-configuration"))` 추가.
- `BoilerplateApplication.java` — `@Modulithic(sharedModules = "shared")` 그대로(이미 적용).

## 5.2 Domain 구현

`boilerplate-notification-domain/src/main/java/.../notification/domain/model/`:

- `NotificationId(UUID value)` — `generate()` 팩토리(UUIDv7).
- `RecipientUserId(UUID value)` — Identity UserId의 ACL 경계 표현.
- `NotificationContent(String subject, String body)` — Compact Constructor 검증(subject 1~200자, body 1~5000자).
- `NotificationChannel { EMAIL }` (현 단계 EMAIL만, 확장 여지).
- `NotificationStatus { PENDING, SENT, FAILED }`.
- `NotificationEvent` sealed interface — `NotificationCreatedEvent`, `NotificationSentEvent`, `NotificationFailedEvent`.
- `NotificationException` sealed class — `AlreadySentException`, `AlreadyFailedException`.
- `Notification` final class:
  - `create(RecipientUserId, NotificationChannel, NotificationContent, Instant)` — PENDING 상태 + `NotificationCreatedEvent` 발행.
  - `markSent(Instant)` / `markFailed(String reason, Instant)` — 상태 전이.
  - `pullDomainEvents()` 표준.
- `domain/model/UUIDv7.java` — Identity와 동일 유틸리티 **중복 작성**. (shared 모듈에 두지 않는 이유: D-1 외부 의존 제로 유지 + 각 BC가 자체 식별자 생성 책임.)

**테스트** (DomainTestBase 상속):
- `NotificationTest` — create / markSent 정상 / 이미 SENT인데 markSent → `AlreadySentException`.
- `NotificationContentTest` — 길이 경계값 검증.

## 5.3 Application 구현

`boilerplate-notification-application/`:

**Output Port** (3분할):
- `LoadNotificationPort` — `findById(NotificationId): Optional<Notification>`.
- `SaveNotificationPort` — `save(Notification): void`.
- `NotificationQueryPort` — `findRecentByRecipient(String userId, int limit): List<NotificationSummary>`.

**Command/Query/Result**:
- `SendUserRegisteredNotificationCommand(String recipientUserId, String userName, String email, String occurredAt)` — Compact Constructor 검증.
- `NotificationSummary(String id, String channel, String state, String subject, Instant createdAt)`.

**UseCase + Service**:
- `SendUserRegisteredNotificationUseCase` + `SendUserRegisteredNotificationService`
  - 로직: `Notification.create(RecipientUserId, EMAIL, NotificationContent("환영합니다", body), clock.instant())` → `savePort.save()`.
- `MarkNotificationSentUseCase` / `MarkNotificationFailedUseCase` — 향후 외부 발송 어댑터에서 호출.

> 본 Phase에서는 **인가 처리 없음**. Integration Event 수신은 Adapter 경계에서 이미 시스템 신뢰 영역. `AuthorizationPolicy` 미사용.

## 5.4 Adapter 구현

### 5.4.1 adapter-input-event (Modulith 핸들러)

`boilerplate-notification-adapter-input-event/`:

```java
@Component
class IdentityUserEventHandler {
  private final SendUserRegisteredNotificationUseCase useCase;

  IdentityUserEventHandler(SendUserRegisteredNotificationUseCase useCase) {
    this.useCase = useCase;
  }

  @ApplicationModuleListener
  void on(UserRegisteredIntegrationEvent event) {
    useCase.execute(new SendUserRegisteredNotificationCommand(
        event.userId().toString(),
        event.userName(),
        event.email(),
        event.occurredAt().toString()));
  }
}
```

> **사전 조건**: `UserRegisteredIntegrationEvent`에 `userName`/`email` 필드가 있는지 확인 필요. Phase 3에서 `userId + occurredAt`만 포함했을 가능성 있음. Phase 5 시작 시 Integration Event 필드 보강 + EventTranslator 동기 갱신.

### 5.4.2 adapter-output-persist

`boilerplate-notification-adapter-output-persist/`:

- `notifications` 테이블 jOOQ 코드젠 (DDL은 §5.6).
- `NotificationPersistenceMapper` — Domain ↔ jOOQ Record 수동 변환.
- `NotificationPersistenceAdapter implements LoadNotificationPort, SaveNotificationPort`
  - AD-3: `save()` 후 `pullDomainEvents().forEach(eventPublisher::publishEvent)` (같은 TX).
  - AD-7: UPDATE `WHERE version = ?`, affected == 0 → `OptimisticLockException`.
  - AD-5: DB 로드 시 `Notification.reconstitute()` 호출.
- `NotificationQueryAdapter implements NotificationQueryPort` — DTO 직접 프로젝션.

## 5.5 Configuration

`boilerplate-notification-configuration/`:

- `NotificationBeanConfiguration` — Clock 주입, 3개 UseCase Bean + TX 프록시(scaffold.md 템플릿).
- `NotificationEventTranslator` — 본 Phase에서는 발신할 Integration Event 없음(미작성 또는 빈 클래스). 향후 `NotificationSentIntegrationEvent` 발신 시 추가.

## 5.6 DDL

`boilerplate-notification-adapter-output-persist/src/main/resources/db/migration/V3__create_notification.sql`:

```sql
CREATE TABLE notifications (
  id              UUID         NOT NULL PRIMARY KEY,
  recipient_id    UUID         NOT NULL,
  channel         VARCHAR(20)  NOT NULL,
  status          VARCHAR(20)  NOT NULL,
  subject         VARCHAR(200) NOT NULL,
  body            TEXT         NOT NULL,
  failure_reason  TEXT,
  sent_at         TIMESTAMPTZ,
  version         BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, created_at DESC);
```

**Flyway 버전 규칙**: Identity V1(users), V2(event_publication) 다음 V3를 사용. 다음 BC 추가 시 V4/V5 등으로 글로벌 시퀀스 유지. 3번째 BC 추가 시 `spring.flyway.locations` namespace 분리 ADR 작성 검토.

## 5.7 Spring Modulith 설정

이미 활성화된 항목(Phase 3):
- `republish-outstanding-events-on-restart: true`
- `completion-mode: delete`
- `event_publication` 테이블(V2)

추가 작업 없음. `@ApplicationModuleListener`는 자동으로 `event_publication`에 적재된 이벤트를 컨슈밍.

## 5.8 검증

### 5.8.1 단위/통합 테스트

| 테스트 파일 | 종류 | 검증 |
|-----------|------|------|
| `NotificationTest` | Domain 단위 | create / markSent / markFailed / 상태 전이 예외 |
| `NotificationContentTest` | Domain VO | 길이 경계값 검증 |
| `SendUserRegisteredNotificationServiceTest` | Application 단위 | Mockito mock으로 SavePort 호출 검증 |
| `NotificationPersistenceAdapterTest` | Adapter 통합 | Testcontainers — save/load 왕복 + AD-7 |
| `IdentityUserEventHandlerTest` | `@ApplicationModuleTest` | `Scenario.publish(UserRegisteredIntegrationEvent)` → DB에 1건 적재 |
| `NotificationModuleIsolationTest` | `@ApplicationModuleTest(STANDALONE)` | 단독 기동 확인 |

### 5.8.2 ModulithStructureTest

자동 통과 예상. Notification 모듈은 Identity를 직접 의존하지 않고 shared-event만 의존. `ApplicationModules.verify()`가 violation 없이 통과해야 함.

### 5.8.3 E2E

기존 `UserRegistrationE2ETest`에 케이스 추가 또는 별도 파일:

```java
@Test
void 회원가입_시_Notification_적재() {
  // 1. POST /api/identity/users로 회원가입
  var body = Map.of("userName", "알림테스트", "email", "notif@test.com", "password", "hashedpw");
  var response = restClient.post().uri(API_PATH)
      .header("Api-Version", API_VERSION)
      .contentType(MediaType.APPLICATION_JSON)
      .body(body)
      .retrieve().toEntity(Map.class);
  var userId = response.getBody().get("id").toString();

  // 2. Awaitility로 event_publication 처리 후 notifications 테이블 확인
  await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
      assertThat(jdbcTemplate.queryForList(
          "SELECT * FROM notifications WHERE recipient_id = ?::uuid", userId))
          .hasSize(1));
}
```

### 5.8.4 5게이트

```bash
./gradlew check --no-daemon
# 기대: BUILD SUCCESSFUL — 약 110+ tasks
# ArchitectureTest, ModulithStructureTest, IdentityUserEventHandlerTest,
# NotificationModuleIsolationTest 모두 통과
```

## Phase 5 변경 파일 매트릭스

**신규 파일** (~30개):
- `boilerplate/notification/{5개 모듈}/build.gradle.kts`
- `boilerplate/notification/boilerplate-notification-domain/src/main/java/.../` — UUIDv7 + 모델 9~10 파일
- `boilerplate/notification/boilerplate-notification-application/src/main/java/.../` — 포트 3 + Command/Result + UseCase/Service 3쌍
- `boilerplate/notification/boilerplate-notification-adapter-input-event/src/main/java/.../IdentityUserEventHandler.java`
- `boilerplate/notification/boilerplate-notification-adapter-output-persist/src/main/java/.../` — Adapter 2 + Mapper + `db/migration/V3__create_notification.sql`
- `boilerplate/notification/boilerplate-notification-configuration/src/main/java/.../NotificationBeanConfiguration.java`
- 테스트 6종 (§5.8.1)

**수정 파일** (5):
- `settings.gradle.kts` — 5개 모듈 등록
- `boilerplate-boot-api/build.gradle.kts` — `:boilerplate-notification-configuration` 추가
- `docs/architecture/context-map.md` — Notification BC 다이어그램 추가
- `boilerplate-shared-event/UserRegisteredIntegrationEvent.java` — `userName`/`email` 필드 보강(현재 부족 시)
- `boilerplate-boot-api/src/test/java/.../UserRegistrationE2ETest.java` — Notification 적재 케이스 추가(또는 별도 파일)

**선택 신규** (1):
- `docs/decisions/0022-notification-bc-as-supporting-domain.md`

## Phase 5 실행 순서

1. **5.0** 전략적 설계 문서화 — `docs/architecture/context-map.md` 갱신, ADR-0022 작성(선택).
2. **사전 조건**: `UserRegisteredIntegrationEvent` 필드 점검 — `userName`/`email` 부족하면 보강 + EventTranslator 동기 갱신.
3. **5.1** 5개 모듈 신설 + settings/boot-api 등록 → `./gradlew :boilerplate-notification-domain:compileJava` 통과.
4. **5.2** Domain 구현 + 테스트.
5. **5.3** Application 구현 + 테스트.
6. **5.6** DDL V3 작성.
7. **5.4.2** persist Adapter + Mapper + 테스트.
8. **5.4.1** input-event 핸들러 + `@ApplicationModuleTest` 테스트.
9. **5.5** Configuration + Bean 와이어링.
10. **5.8.3** E2E 케이스 추가.
11. `./gradlew check` 5게이트 통과 확인.
12. 수동 부팅 + smoke test:
    ```bash
    docker compose up -d
    ./gradlew :boilerplate-boot-api:bootRun
    curl -X POST localhost:8080/api/identity/users \
      -H "Content-Type: application/json" -H "Api-Version: 2026-05-08" \
      -d '{"userName":"홍","email":"notif@test.com","password":"hashed"}'
    psql -h localhost -U boilerplate -d boilerplate -c "SELECT * FROM notifications;"
    # 기대: 1건 (recipient_id = 위 회원가입의 userId)
    ```

## Phase 5 위험 요소

| 위험 | 대응 |
|------|------|
| `UserRegisteredIntegrationEvent` 필드 부족 | Phase 5 Step 2에서 선행 보강. EventTranslator도 동기 갱신. |
| `@ApplicationModuleListener` 비동기 처리 시점 비결정성 | 테스트에 Awaitility 5초 폴링. Spring Modulith는 TX 커밋 후 fire — 충분. |
| Flyway V3 충돌 | 글로벌 시퀀스 유지(V1/V2/V3). 3번째 BC 추가 시 namespace 분리 ADR 작성. |
| Modulith STANDALONE 모드에서 shared-event 의존 | shared-event는 sharedModules에 등록되어 있어 STANDALONE 부팅 시 자동 포함. 추가 작업 없음. |
