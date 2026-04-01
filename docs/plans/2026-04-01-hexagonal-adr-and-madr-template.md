# Hexagonal ADR + MADR 4.0 템플릿 표준화 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hexagonal Architecture/CQRS 채택 근거를 ADR로 문서화하고, MADR 4.0 템플릿을 프로젝트 표준으로 도입한다.

**Architecture:** 문서 전용 작업. ADR 2개(0005, 0006), MADR 4.0 템플릿 2개(full, minimal), rules/CLAUDE.md 업데이트. 코드 변경 없음.

**Tech Stack:** Markdown (MADR 4.0 format)

**Spec:** `docs/specs/2026-04-01-hexagonal-adr-and-madr-template-design.md`

---

## File Structure

| Action | File | Responsibility |
|--------|------|---------------|
| Create | `docs/decisions/0000-template.md` | MADR 4.0 full bare 템플릿 |
| Create | `docs/decisions/0000-template-minimal.md` | MADR 4.0 minimal bare 템플릿 |
| Create | `docs/decisions/0005-hexagonal-architecture-and-cqrs.md` | Hexagonal + CQRS 채택 ADR |
| Create | `docs/decisions/0006-package-structure-and-naming.md` | 패키지 구조 및 네이밍 컨벤션 ADR |
| Modify | `.claude/rules/architecture.md:18,40,63` | `[ADR-0005]`, `[ADR-0006]` 태그 부착 |
| Modify | `.claude/rules/rules-maintenance.md:29-40` | MADR 4.0 기준으로 갱신 |
| Modify | `.claude/CLAUDE.md:44-49` | ADR 참조 테이블에 0005, 0006 추가 |

---

### Task 1: MADR 4.0 full bare 템플릿

**Files:**
- Create: `docs/decisions/0000-template.md`

- [ ] **Step 1: 템플릿 파일 작성**

```markdown
---
status: {accepted | deprecated | superseded by ADR-NNNN}
date: YYYY-MM-DD
decision-makers:
consulted:
informed:
---

# {짧은 제목, 해결한 문제와 선택한 해결책을 대표}

## Context and Problem Statement

{2-3 문장으로 문제 상황과 결정이 필요한 이유를 설명}

## Decision Drivers

* {driver 1}
* {driver 2}

## Considered Options

* {option 1}
* {option 2}
* {option 3}

## Decision Outcome

Chosen option: "{선택한 옵션}", because {1-2 문장 근거}.

### Consequences

* Good, because {긍정적 결과}
* Bad, because {부정적 결과 또는 트레이드오프}

### Confirmation

{이 결정이 올바르게 구현되었는지 확인하는 방법. 자동 검증(테스트, lint) 또는 수동 리뷰 기준.}

## Pros and Cons of the Options

### {옵션 1}

* Good, because {장점}
* Neutral, because {중립적 특성}
* Bad, because {단점}

### {옵션 2}

* Good, because {장점}
* Bad, because {단점}

## More Information

{관련 ADR, rules 파일, 외부 문서 링크 등}
```

- [ ] **Step 2: Commit**

```bash
git add docs/decisions/0000-template.md
git commit -m "docs: add MADR 4.0 full bare template"
```

---

### Task 2: MADR 4.0 minimal bare 템플릿

**Files:**
- Create: `docs/decisions/0000-template-minimal.md`

- [ ] **Step 1: 템플릿 파일 작성**

```markdown
---
status: {accepted | deprecated | superseded by ADR-NNNN}
date: YYYY-MM-DD
decision-makers:
---

# {짧은 제목, 해결한 문제와 선택한 해결책을 대표}

## Context and Problem Statement

{2-3 문장으로 문제 상황과 결정이 필요한 이유를 설명}

## Decision Outcome

Chosen option: "{선택한 옵션}", because {1-2 문장 근거}.
```

- [ ] **Step 2: Commit**

```bash
git add docs/decisions/0000-template-minimal.md
git commit -m "docs: add MADR 4.0 minimal bare template"
```

---

### Task 3: rules/rules-maintenance.md MADR 4.0 갱신

**Files:**
- Modify: `.claude/rules/rules-maintenance.md:29-40`

- [ ] **Step 1: MADR 작성 형식 섹션 교체**

`.claude/rules/rules-maintenance.md`의 `## MADR 작성 형식` 섹션을 아래로 교체:

```markdown
## MADR 작성 형식 [MADR 4.0]

`docs/decisions/NNNN-<kebab-case-title>.md` 형식으로 저장.
번호는 앞에 있는 최대 번호 + 1.

템플릿:
- full: `docs/decisions/0000-template.md` — 대부분의 결정에 사용
- minimal: `docs/decisions/0000-template-minimal.md` — 검토 옵션이 2개 이하이고 트레이드오프가 경미한 결정

frontmatter 필수 필드:
- `status`: accepted / deprecated / superseded by ADR-NNNN
- `date`: YYYY-MM-DD
- `decision-makers`: 결정 참여자

full 템플릿 필수 섹션:
- Context and Problem Statement
- Considered Options (최소 2개)
- Decision Outcome + Consequences
```

- [ ] **Step 2: Commit**

```bash
git add .claude/rules/rules-maintenance.md
git commit -m "docs: update rules-maintenance to MADR 4.0 standard"
```

---

### Task 4: ADR-0005 Hexagonal Architecture + CQRS

**Files:**
- Create: `docs/decisions/0005-hexagonal-architecture-and-cqrs.md`

- [ ] **Step 1: ADR 파일 작성**

```markdown
---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# Hexagonal Architecture + CQRS 경계 분리 채택

## Context and Problem Statement

Spring Boot 멀티모듈 보일러플레이트의 아키텍처 패턴을 결정해야 한다.
비즈니스 로직이 프레임워크/인프라에 결합되면 테스트가 어렵고, 인프라 교체 시 비즈니스 로직까지 수정해야 한다.
또한 읽기/쓰기 로직이 혼재하면 복잡도가 급증하고, 독립적인 확장이 어렵다.

## Decision Drivers

* 비즈니스 로직의 프레임워크 독립성
* 외부 의존성 격리를 통한 테스트 용이성
* adapter 교체만으로 인프라 변경 가능한 구조
* 보일러플레이트로서의 범용 적용 가능성
* 읽기/쓰기 로직의 독립적 확장 기반

## Considered Options

* A. Hexagonal Architecture (Ports & Adapters) + CQRS 경계 분리
* B. Layered Architecture (Controller → Service → Repository)
* C. Clean Architecture (Uncle Bob 동심원 레이어)

## Decision Outcome

Chosen option: "A. Hexagonal Architecture + CQRS 경계 분리", because
Port interface를 통해 외부 의존성을 격리하여 테스트가 용이하고,
domain/application이 프레임워크에 오염되지 않아 인프라 교체에도 코어 로직이 불변하며,
CQRS 경계를 ArchUnit으로 강제하여 읽기/쓰기 로직의 독립성을 보장한다.

**Hexagonal 채택 이유:**

* 테스트 용이성 — Port interface를 통해 외부 의존성 격리. domain/application 단위 테스트에 Spring Context 불필요
* 도메인 보호 — domain/application이 Spring/JPA에 오염되지 않아 프레임워크 교체에도 코어 로직 불변
* 인프라 교체 자유도 — adapter 교체만으로 DB, 캐시, 메시징 등 기술 스택 변경 가능
* 보일러플레이트 범용성 — adapter를 추가/교체하는 구조라 다양한 프로젝트에 적용 가능

**CQRS 경계 분리 이유:**

* 읽기/쓰기 로직 분리로 각각의 복잡도를 독립 관리
* query service가 command port에 의존하면 조회에서 쓰기 부수효과 발생 가능 — ArchUnit으로 원천 차단
* 향후 읽기 전용 replica나 캐시 최적화 시 변경 범위 최소화
* 보일러플레이트로서 구조적 가이드를 자동화(ArchUnit)하여 fork 사용자의 실수 방지

**의존성 방향:**

```
Inbound Adapter ──→ [Port] ──→ Application(UseCase) ──→ [Port] ──→ Outbound Adapter
                                      │
                                      ↓
                               Domain (순수 Java)
```

### Consequences

* Good, because domain/application 단위 테스트에 Spring Context가 불필요하여 테스트 속도와 격리성이 높다
* Good, because adapter를 교체/추가하는 것만으로 인프라 기술 스택을 변경할 수 있다
* Good, because CQRS 경계를 ArchUnit으로 자동 검증하여 읽기/쓰기 로직의 독립성을 보장한다
* Bad, because adapter 간 직접 통신이 불가하여 반드시 application port를 경유해야 한다
* Bad, because Port/Adapter 보일러플레이트 코드가 Layered 대비 증가한다

### Confirmation

ArchUnit 테스트가 의존성 방향과 CQRS 경계를 자동 검증한다:
* `DomainArchitectureTest` — domain 레이어의 Spring/JPA 의존 금지
* `ApplicationArchitectureTest` — application 레이어의 Spring 의존 금지, query→command port 의존 금지

## Pros and Cons of the Options

### B. Layered Architecture

* Good, because 단순하고 학습 곡선이 낮다
* Good, because 소규모 프로젝트에서 빠르게 시작할 수 있다
* Bad, because 비즈니스 로직이 프레임워크(Spring/JPA)에 결합된다
* Bad, because 인프라 교체 시 전 레이어 수정이 필요하다
* Bad, because 단위 테스트에 Spring Context가 필요하여 테스트 속도가 느리다

### C. Clean Architecture

* Good, because Hexagonal과 원칙이 유사하며 의존성 규칙이 명확하다
* Good, because Entity/UseCase/Interface Adapter/Framework 레이어 구분이 엄격하다
* Bad, because 동심원 4레이어가 Hexagonal의 3레이어보다 복잡하여 보일러플레이트로는 과도하다
* Bad, because Hexagonal 대비 실질적 이점이 크지 않으면서 구조적 오버헤드가 증가한다

## More Information

* 관련 규칙: `.claude/rules/architecture.md` (의존성 방향 규칙, 금지 규칙)
* 관련 ADR: ADR-0006 (패키지 구조 및 네이밍 컨벤션)
```

- [ ] **Step 2: Commit**

```bash
git add docs/decisions/0005-hexagonal-architecture-and-cqrs.md
git commit -m "docs: add ADR-0005 hexagonal architecture and CQRS"
```

---

### Task 5: ADR-0006 패키지 구조 및 네이밍 컨벤션

**Files:**
- Create: `docs/decisions/0006-package-structure-and-naming.md`

- [ ] **Step 1: ADR 파일 작성**

```markdown
---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 패키지 구조 및 네이밍 컨벤션

## Context and Problem Statement

Hexagonal Architecture를 채택했으므로(ADR-0005), 각 레이어의 패키지 경로와 클래스 네이밍 규칙을 통일해야 한다.
일관된 컨벤션이 없으면 도메인 경계가 무너지고, ArchUnit 규칙 작성이 어려워지며, AI와 사람 모두 코드 탐색이 비효율적이 된다.

## Decision Drivers

* AI와 사람 모두 클래스 이름만으로 역할을 파악할 수 있어야 함
* ArchUnit으로 자동 검증 가능한 패턴이어야 함
* 베이스 패키지(`io.github.ppzxc.template`) 하위에서 모듈과 패키지가 1:1 매핑

## Considered Options

* A. 접미사 기반 컨벤션 — `*UseCase`, `*Port`, `*Service`, `*Adapter`, `*JpaEntity`
* B. 패키지 기반 구분만 — 클래스 이름에 접미사 없이 패키지 경로로만 역할 구분

## Decision Outcome

Chosen option: "A. 접미사 기반 컨벤션", because
클래스명에 역할이 인코딩되어 IDE 검색과 ArchUnit 패턴 매칭이 용이하고,
AI가 코드를 생성할 때 일관된 네이밍을 적용할 수 있다.

**패키지 트리:**

```
io.github.ppzxc.template
├── domain/                                  ← template-domain
├── application/                             ← template-application
│   ├── port/input/command/   *UseCase       ← Inbound Command Port (interface)
│   ├── port/input/query/     *Query         ← Inbound Query Port (interface)
│   ├── port/output/command/  *Port          ← Outbound Command Port (interface)
│   ├── port/output/query/    *Port          ← Outbound Query Port (interface)
│   ├── port/output/shared/   *Port          ← Shared Infra Port (interface)
│   ├── service/command/      *Service       ← Command UseCase 구현체
│   └── service/query/        *Service       ← Query UseCase 구현체
├── adapter/input/api/                       ← template-adapter-input-api
├── adapter/input/ws/                        ← template-adapter-input-ws
├── adapter/output/persist/                  ← template-adapter-output-persist
└── adapter/output/cache/                    ← template-adapter-output-cache
```

**네이밍 규칙:**

| 타입 | 접미사 패턴 | 예시 |
|------|------------|------|
| Inbound Command Port | `*UseCase` interface | `CreateOrderUseCase` |
| Inbound Query Port | `*Query` interface | `FindOrderQuery` |
| Outbound Port | `*Port` interface | `SaveOrderPort`, `FindOrderPort`, `LockPort` |
| UseCase 구현체 | `*Service` | `CreateOrderService` |
| JPA Entity | `*JpaEntity` | `OrderJpaEntity` |
| JPA Repository | `*JpaRepository` | `OrderJpaRepository` |
| Outbound Adapter | `*Adapter` | `OrderPersistAdapter` |
| Controller | `*Controller` | `OrderController` |
| DTO (Request) | `*Request` | `CreateOrderRequest` |
| DTO (Response) | `*Response` | `OrderResponse` |

**Port interface 강제 규칙:**

* `..port.output..*` 패키지의 모든 타입은 반드시 interface
* `..port.input.command..*UseCase`는 반드시 interface
* `..port.input.query..*Query`는 반드시 interface

### Consequences

* Good, because 클래스명만으로 역할을 즉시 파악할 수 있다
* Good, because ArchUnit에서 접미사 패턴으로 규칙을 강제할 수 있다
* Good, because AI가 코드 생성 시 일관된 네이밍을 적용할 수 있다
* Bad, because 클래스 이름이 길어진다 (예: `OrderPersistAdapter`)

### Confirmation

ArchUnit 테스트(`ApplicationArchitectureTest`)가 Port interface 규칙을 자동 검증한다.

## Pros and Cons of the Options

### B. 패키지 기반 구분만

* Good, because 클래스 이름이 짧다
* Bad, because 같은 이름의 클래스가 여러 패키지에 존재할 수 있다 (예: `OrderRepository`가 persist와 cache 모두에)
* Bad, because IDE 검색 시 동일 이름 충돌로 혼란이 발생한다
* Bad, because ArchUnit에서 클래스명 패턴 매칭이 불가하여 패키지 경로만으로 규칙을 강제해야 한다

## More Information

* 관련 규칙: `.claude/rules/architecture.md` (패키지 구조 규칙, Port 인터페이스 규칙)
* 관련 규칙: `.claude/rules/coding-style.md` (네이밍 규칙 테이블)
* 관련 ADR: ADR-0005 (Hexagonal Architecture + CQRS 경계 분리)
```

- [ ] **Step 2: Commit**

```bash
git add docs/decisions/0006-package-structure-and-naming.md
git commit -m "docs: add ADR-0006 package structure and naming convention"
```

---

### Task 6: rules/architecture.md ADR 태그 부착

**Files:**
- Modify: `.claude/rules/architecture.md:18,32,40,63`

- [ ] **Step 1: 의존성 방향 규칙 섹션에 태그 추가**

`.claude/rules/architecture.md` 18번줄:

변경 전:
```markdown
## 의존성 방향 규칙
```

변경 후:
```markdown
## 의존성 방향 규칙 [ADR-0005]
```

- [ ] **Step 2: 금지 규칙 섹션 제목에 태그 추가**

32번줄:

변경 전:
```markdown
### 금지 규칙
```

변경 후:
```markdown
### 금지 규칙 [ADR-0005]
```

- [ ] **Step 3: 패키지 구조 규칙 섹션에 태그 추가**

40번줄:

변경 전:
```markdown
## 패키지 구조 규칙
```

변경 후:
```markdown
## 패키지 구조 규칙 [ADR-0006]
```

- [ ] **Step 4: Port 인터페이스 규칙 섹션에 태그 추가**

63번줄:

변경 전:
```markdown
## Port 인터페이스 규칙
```

변경 후:
```markdown
## Port 인터페이스 규칙 [ADR-0006]
```

- [ ] **Step 5: Commit**

```bash
git add .claude/rules/architecture.md
git commit -m "docs: add ADR-0005/0006 tags to architecture rules"
```

---

### Task 7: CLAUDE.md ADR 참조 테이블 업데이트

**Files:**
- Modify: `.claude/CLAUDE.md:44-49`

- [ ] **Step 1: ADR 참조 테이블에 0005, 0006 추가**

`.claude/CLAUDE.md`의 ADR 참조 테이블:

변경 전:
```markdown
| ADR | 주제 |
|-----|------|
| ADR-0001 | 모듈 레이아웃 (플랫 구조, 8개 모듈) |
| ADR-0002 | 코드 품질 도구 (Spotless, Checkstyle, ErrorProne, NullAway) |
| ADR-0003 | 아키텍처 테스트 전략 (ArchUnit) |
| ADR-0004 | CI 파이프라인 전략 (Lefthook, GitHub Actions, JaCoCo, OpenRewrite) |
```

변경 후:
```markdown
| ADR | 주제 |
|-----|------|
| ADR-0001 | 모듈 레이아웃 (플랫 구조, 8개 모듈) |
| ADR-0002 | 코드 품질 도구 (Spotless, Checkstyle, ErrorProne, NullAway) |
| ADR-0003 | 아키텍처 테스트 전략 (ArchUnit) |
| ADR-0004 | CI 파이프라인 전략 (Lefthook, GitHub Actions, JaCoCo, OpenRewrite) |
| ADR-0005 | Hexagonal Architecture + CQRS 경계 분리 |
| ADR-0006 | 패키지 구조 및 네이밍 컨벤션 |
```

- [ ] **Step 2: Commit**

```bash
git add .claude/CLAUDE.md
git commit -m "docs: add ADR-0005/0006 to CLAUDE.md reference table"
```

---

## Verification

모든 Task 완료 후:

1. **파일 존재 확인:**
```bash
ls docs/decisions/0000-template.md docs/decisions/0000-template-minimal.md \
   docs/decisions/0005-hexagonal-architecture-and-cqrs.md \
   docs/decisions/0006-package-structure-and-naming.md
```

2. **ADR 태그 정합성 확인:**
```bash
grep -n 'ADR-0005' .claude/rules/architecture.md
# 예상: 의존성 방향 규칙, 금지 규칙 섹션에서 매칭

grep -n 'ADR-0006' .claude/rules/architecture.md
# 예상: 패키지 구조 규칙, Port 인터페이스 규칙 섹션에서 매칭
```

3. **CLAUDE.md ADR 테이블 확인:**
```bash
grep 'ADR-0005\|ADR-0006' .claude/CLAUDE.md
# 예상: 2줄 매칭
```

4. **rules-maintenance.md에 MADR 4.0 반영 확인:**
```bash
grep 'MADR 4.0' .claude/rules/rules-maintenance.md
# 예상: 1줄 매칭
```
