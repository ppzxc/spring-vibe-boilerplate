# Context Map — Boilerplate System

> **Living Document.** BC 추가·변경 시 갱신한다.
>
> 분리된 산출물:
> - `ubiquitous-language-{bc}.md` — BC별 UL 사전 (예: `ubiquitous-language-identity.md`)
> - `module-bc-mapping.md` — Gradle 모듈 ↔ BC ↔ 계층 매핑
> - `strategic-design-changelog.md` — 변경 이력
>
> 결정의 배경은 `docs/decisions/`(ADR)에 있다.

---

## 1. Subdomain 분류

| Bounded Context | Subdomain 유형 | 이유 |
|----------------|--------------|------|
| **Identity BC** | Core Domain | 사내 IDP + Resource Server의 핵심 차별화 요소. 인증/인가 정책이 비즈니스 그 자체. 없으면 서비스가 성립하지 않는다. |
| **Notification BC** | Supporting Domain | 알림 적재·발송은 비즈니스를 지원하는 공통 인프라. 경쟁 우위 요소가 아니며 외부 채널(SMTP, SMS 등) 연동 확장을 위한 ACL 경계로 분리. |
| **Audit BC** | Supporting Domain | 감사 로그 적재는 컴플라이언스/운영 가시성을 지원하는 인프라. 비즈니스 차별화 요소가 아니며, Identity 이벤트를 Conformist 방식으로 수용해 append-only로 적재. |

> DDD 깊이 결정:
> - Identity: **Full DDD** — Rich Domain Model, Aggregate, VO(record), Domain Event(sealed interface), Domain Exception(sealed class), Output Port 3분할(Load/Save/Query).
> - Notification: **Simplified DDD** — Aggregate + CRUD 중심, 상태 전이 규칙은 단순(PENDING → SENT/FAILED).
> - Audit: **Simplified DDD** — append-only Aggregate, 상태 전이 0, INSERT만 수행. Notification BC 패턴 미러링.

---

## 2. Context Map (BC 간 관계)

### 현재 상태 (Phase 6 완료 기준)

```
┌─────────────────────────────┐     Published Language      ┌──────────────────────────┐
│        Identity BC          │ ─────────────────────────→ │     Notification BC      │
│   (Core Domain, Upstream)   │  UserRegisteredIntegration  │  (Supporting, Downstream)│
│                             │  Event                      │   ACL 패턴 적용           │
│  User, Role, Permission,    │                             │                          │
│  Client, Credential         │                             │  Notification, Channel,  │
└─────────────────────────────┘                             │  Status, RecipientUserId │
              │                                             └──────────────────────────┘
              │ Published Language
              │ (shared-event 모듈)
              │
              │ UserRegisteredIntegrationEvent
              │ (Conformist)
              ↓
┌─────────────────────────────┐
│         Audit BC            │
│   (Supporting, Downstream)  │
│   Conformist + ACL VO       │
│                             │
│  AuditLog, AuditedUserId,   │
│  AuditEventType, AuditPayload│
└─────────────────────────────┘

              │ Published Language 컨테이너
              ↓
┌─────────────────────────────┐
│  boilerplate-shared-event   │
│  (Published Language 컨테이너)│
│                             │
│  IntegrationEvent (marker)  │
│  UserRegisteredIntegration  │
│  Event                      │
└─────────────────────────────┘
```

### 통합 패턴

| 관계 | 패턴 | 설명 |
|------|------|------|
| Identity → Notification | Published Language + ACL | Identity BC가 `UserRegisteredIntegrationEvent`(shared-event)를 발행, Notification BC의 `IdentityUserEventHandler`가 수신해 `SendUserRegisteredNotificationCommand`로 번역(ACL) |
| Identity → Audit | Published Language + Conformist + ACL VO | Identity BC가 `UserRegisteredIntegrationEvent`를 발행, Audit BC의 `IdentityUserRegisteredEventHandler`가 수신해 `RecordUserRegisteredAuditCommand`로 번역. Identity `UserId` → Audit BC 내부 `AuditedUserId` ACL VO로 격리 |
