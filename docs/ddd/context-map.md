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

> DDD 깊이 결정: **Full DDD** — Rich Domain Model, Aggregate, VO(record), Domain Event(sealed interface), Domain Exception(sealed class), Output Port 3분할(Load/Save/Query).

---

## 2. Context Map (BC 간 관계)

### 현재 상태 (Phase 3 완료 기준)

```
┌─────────────────────────────┐
│        Identity BC          │
│   (Core Domain, Upstream)   │
│                             │
│  User, Role, Permission,    │
│  Client, Credential         │
└─────────────────────────────┘
              │
              │ Published Language
              │ (shared-event 모듈)
              ↓
┌─────────────────────────────┐
│  boilerplate-shared-event   │
│  (Published Language 컨테이너)│
│                             │
│  IntegrationEvent (marker)  │
└─────────────────────────────┘
```

### Phase 5 이후 예정 (Notification BC 추가 시)

```
┌─────────────────────────────┐     Published Language      ┌──────────────────────────┐
│        Identity BC          │ ─────────────────────────→ │     Notification BC      │
│   (Core Domain, Upstream)   │  UserRegisteredIntegration  │   (Supporting, Downstream│
│                             │  Event                      │   ACL 패턴 적용)          │
└─────────────────────────────┘                             └──────────────────────────┘
              │
              │ Published Language
              ↓
┌─────────────────────────────┐
│         Audit BC            │
│   (Supporting, Downstream)  │
└─────────────────────────────┘
```

### 통합 패턴

| 관계 | 패턴 | 설명 |
|------|------|------|
| Identity → Notification | Published Language + ACL | Identity BC가 Integration Event(shared-event)를 발행, Notification BC는 자체 모델로 번역(ACL) |
| Identity → Audit | Published Language | Audit BC는 Identity Event를 Conformist 방식으로 수용 |
