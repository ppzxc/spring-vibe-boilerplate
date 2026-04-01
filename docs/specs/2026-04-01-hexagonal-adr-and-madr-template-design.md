# Hexagonal Architecture ADR + MADR 4.0 템플릿 표준화 스펙

## 요약

Hexagonal Architecture와 CQRS 채택 근거를 ADR로 문서화하고, MADR 4.0 템플릿을 프로젝트 표준으로 도입한다.

## 배경

- `rules/architecture.md`에 의존성 방향, 금지 규칙, 패키지 구조, CQRS 경계 등 제약(what)이 상세히 기술되어 있음
- 이 제약들의 근거(why)를 담을 ADR이 없는 상태. 기존 ADR-0001은 디렉토리 구조(플랫 배치) 결정이지 아키텍처 패턴 결정이 아님
- ADR 작성 시 참조할 MADR 템플릿이 프로젝트에 없음
- 문서의 주요 독자는 AI(Claude Code 컨텍스트 주입)와 사람(팀원, fork 개발자) 모두

## 산출물

### 1. MADR 4.0 템플릿 (2개)

| 파일 | 설명 |
|------|------|
| `docs/decisions/0000-template.md` | MADR 4.0 full bare 템플릿 |
| `docs/decisions/0000-template-minimal.md` | MADR 4.0 minimal bare 템플릿 |

**full 템플릿 섹션:**

```
---
status: {accepted | deprecated | superseded by ADR-NNNN}
date: YYYY-MM-DD
decision-makers:
consulted:
informed:
---

# {짧은 제목}

## Context and Problem Statement

## Decision Drivers

## Considered Options

## Decision Outcome
Chosen option: "", because

### Consequences

### Confirmation

## Pros and Cons of the Options

### {옵션 제목}

## More Information
```

**minimal 템플릿 섹션:**

```
---
status: {accepted | deprecated | superseded by ADR-NNNN}
date: YYYY-MM-DD
decision-makers:
---

# {짧은 제목}

## Context and Problem Statement

## Decision Outcome
Chosen option: "", because
```

**사용 기준:**
- 대부분의 결정: full 템플릿
- minimal 사용 가능 조건: 검토 옵션이 2개 이하이고 트레이드오프가 경미한 결정 (예: 네이밍 컨벤션, 포맷 설정)

### 2. ADR-0005: Hexagonal Architecture + CQRS 경계 분리 채택

**형식:** MADR 4.0 full

**Context and Problem Statement:**
Spring Boot 멀티모듈 보일러플레이트의 아키텍처 패턴을 결정해야 한다. 비즈니스 로직이 프레임워크/인프라에 결합되면 테스트가 어렵고, 인프라 교체 시 비즈니스 로직까지 수정해야 한다.

**Decision Drivers:**
- 비즈니스 로직의 프레임워크 독립성
- 외부 의존성 격리를 통한 테스트 용이성
- adapter 교체만으로 인프라 변경 가능한 구조
- 보일러플레이트로서의 범용 적용 가능성
- 읽기/쓰기 로직의 독립적 확장 기반

**Considered Options:**
- A. Hexagonal Architecture (Ports & Adapters) + CQRS 경계 분리 (채택)
- B. Layered Architecture (Controller -> Service -> Repository)
- C. Clean Architecture (동심원 레이어)

**Decision Outcome:** A 채택.

Hexagonal 채택 이유:
- 테스트 용이성: Port interface를 통해 외부 의존성 격리. domain/application 단위 테스트에 Spring Context 불필요
- 도메인 보호: domain/application이 Spring/JPA에 오염되지 않아 프레임워크 교체에도 코어 로직 불변
- 인프라 교체 자유도: adapter 교체만으로 DB, 캐시, 메시징 등 기술 스택 변경 가능
- 보일러플레이트 범용성: adapter를 추가/교체하는 구조라 다양한 프로젝트에 적용 가능

CQRS 경계 분리 이유:
- 읽기/쓰기 로직 분리로 각각의 복잡도를 독립 관리
- query service가 command port에 의존하면 조회에서 쓰기 부수효과 발생 가능 - ArchUnit으로 원천 차단
- 향후 읽기 전용 replica나 캐시 최적화 시 변경 범위 최소화
- 보일러플레이트로서 구조적 가이드를 자동화(ArchUnit)하여 fork 사용자의 실수 방지

의존성 방향 다이어그램:
```
Inbound Adapter -> [Port] -> Application(UseCase) -> [Port] -> Outbound Adapter
                             Domain (순수 Java)
```

Consequences:
- Good: 테스트 용이성, 도메인 보호, 인프라 교체 자유도, CQRS 확장 기반
- Bad: adapter 간 직접 통신 불가(반드시 application port 경유), Port/Adapter 보일러플레이트 코드 증가

Confirmation:
- ArchUnit 테스트(DomainArchitectureTest, ApplicationArchitectureTest)가 의존성 방향과 CQRS 경계를 자동 검증

**Pros and Cons of the Options:**

B. Layered Architecture:
- Good: 단순하고 학습 곡선이 낮음
- Bad: 비즈니스 로직이 프레임워크에 결합. 인프라 교체 시 전 레이어 수정 필요
- Bad: 테스트에 Spring Context 필요

C. Clean Architecture:
- Good: Hexagonal과 원칙 유사, 의존성 규칙 명확
- Bad: 동심원 레이어가 많아 보일러플레이트로는 과도. Entity/UseCase/Interface Adapter/Framework 4레이어가 Hexagonal의 3레이어보다 복잡

### 3. ADR-0006: 패키지 구조 및 네이밍 컨벤션

**형식:** MADR 4.0 full

**Context and Problem Statement:**
Hexagonal Architecture를 채택했으므로(ADR-0005), 각 레이어의 패키지 경로와 클래스 네이밍 규칙을 통일해야 한다. 일관된 컨벤션이 없으면 도메인 경계가 무너지고 ArchUnit 규칙 작성이 어려워진다.

**Decision Drivers:**
- AI와 사람 모두 클래스 이름만으로 역할을 파악할 수 있어야 함
- ArchUnit으로 자동 검증 가능한 패턴이어야 함
- 베이스 패키지(`io.github.ppzxc.template`) 하위에서 모듈과 패키지가 1:1 매핑

**Considered Options:**
- A. 접미사 기반 컨벤션 (채택) - `*UseCase`, `*Port`, `*Service`, `*Adapter`, `*JpaEntity`
- B. 패키지 기반 구분만 - 클래스 이름에 접미사 없이 패키지 경로로만 역할 구분

**Decision Outcome:** A 채택.

패키지 트리:
```
io.github.ppzxc.template
├── domain/
├── application/
│   ├── port/input/command/   *UseCase (interface)
│   ├── port/input/query/     *Query (interface)
│   ├── port/output/command/  *Port (interface)
│   ├── port/output/query/    *Port (interface)
│   ├── port/output/shared/   *Port (interface)
│   ├── service/command/      *Service
│   └── service/query/        *Service
├── adapter/input/api/
├── adapter/input/ws/
├── adapter/output/persist/
└── adapter/output/cache/
```

네이밍 규칙:
| 타입 | 패턴 | 예시 |
|------|------|------|
| Inbound Command Port | `*UseCase` interface | `CreateOrderUseCase` |
| Inbound Query Port | `*Query` interface | `FindOrderQuery` |
| Outbound Port | `*Port` interface | `SaveOrderPort` |
| UseCase 구현체 | `*Service` | `CreateOrderService` |
| JPA Entity | `*JpaEntity` | `OrderJpaEntity` |
| JPA Repository | `*JpaRepository` | `OrderJpaRepository` |
| Outbound Adapter | `*Adapter` | `OrderPersistAdapter` |
| Controller | `*Controller` | `OrderController` |
| DTO (Request) | `*Request` | `CreateOrderRequest` |
| DTO (Response) | `*Response` | `OrderResponse` |

Port interface 강제 규칙:
- `..port.output..*` 패키지의 모든 타입은 interface
- `..port.input.command..*UseCase` 는 interface
- `..port.input.query..*Query` 는 interface

Consequences:
- Good: 클래스명만으로 역할 즉시 파악, ArchUnit 패턴 매칭 용이, AI가 코드 생성 시 일관된 네이밍 적용
- Bad: 이름이 길어짐 (예: `OrderPersistAdapter`)

Pros and Cons - B. 패키지 기반 구분만:
- Good: 클래스 이름이 짧음
- Bad: 같은 이름의 클래스가 여러 패키지에 존재 가능 (예: `OrderRepository`가 persist와 cache에 모두), IDE 검색 시 혼란
- Bad: ArchUnit에서 클래스명 패턴 매칭 불가, 패키지 경로만으로 규칙 강제해야 함

### 4. rules 및 CLAUDE.md 업데이트

**rules/architecture.md 태그 부착:**
| 섹션 | 태그 |
|------|------|
| 의존성 방향 규칙 | `[ADR-0005]` |
| 금지 규칙 | `[ADR-0005]` |
| 패키지 구조 규칙 | `[ADR-0006]` |
| Port 인터페이스 규칙 | `[ADR-0006]` |

**rules/rules-maintenance.md 업데이트:**
- MADR 작성 형식 섹션을 MADR 4.0 기준으로 갱신
- 템플릿 파일 경로 참조 추가
- full vs minimal 사용 기준 명시

**CLAUDE.md ADR 참조 테이블 추가:**
| ADR | 주제 |
|-----|------|
| ADR-0005 | Hexagonal Architecture + CQRS 경계 분리 |
| ADR-0006 | 패키지 구조 및 네이밍 컨벤션 |

### 5. 기존 ADR(0001~0004) MADR 4.0 마이그레이션

이번 스코프에서는 하지 않음. 별도 작업으로 진행.
