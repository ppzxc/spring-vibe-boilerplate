# Architecture Decision Records

본 디렉터리는 프로젝트의 중요한 아키텍처 결정을 MADR 4.0 형식으로 기록한다.
업계 표준과 다른 결정은 모두 ADR로 문서화되어야 한다.

## 인덱스

### 뼈대 아키텍처 (0001-0005)

| ADR | 제목 | 상태 |
|-----|------|------|
| [0001](0001-purist-ddd-domain-zero-external-dependency.md) | 순수주의 DDD — Domain 모듈 외부 의존 제로 | Accepted |
| [0002](0002-hexagonal-architecture-port-adapter-separation.md) | 헥사고날 아키텍처 — Port/Adapter 4계층 분리 | Accepted |
| [0003](0003-gradle-multimodule-spring-modulith-hybrid.md) | Gradle 멀티모듈 + Spring Modulith 하이브리드 | Accepted |
| [0004](0004-cqrs-level1-class-separation-same-db.md) | CQRS Level 1 — 클래스 분리, 동일 DB | Accepted |
| [0005](0005-jooq-over-jpa.md) | jOOQ 선택, JPA 배제 | Accepted |

### 규칙 결정 — Full (0006-0008)

| ADR | 제목 | 관련 규칙 | 상태 |
|-----|------|----------|------|
| [0006](0006-no-repository-interface-in-domain.md) | Domain에 Repository 인터페이스 금지 | D-10 | Accepted |
| [0007](0007-domain-service-no-port-call.md) | Domain Service에서 Port 호출 금지 | D-11 | Accepted |
| [0008](0008-no-transactional-in-application.md) | Application Service에 @Transactional 금지 | A-4, T-1 | Accepted |

### 규칙 결정 — Bare Minimal (0009-0015)

| ADR | 제목 | 관련 규칙 | 상태 |
|-----|------|----------|------|
| [0009](0009-input-port-interface-mandatory.md) | Input Port 인터페이스 필수 | A-2 | Accepted |
| [0010](0010-no-direct-usecase-to-usecase-call.md) | UseCase 간 직접 호출 금지 | A-8 | Accepted |
| [0011](0011-uuidv7-mandatory.md) | UUIDv7 강제 | D-7 (식별자) | Accepted |
| [0012](0012-scopedvalue-no-threadlocal.md) | ScopedValue (ThreadLocal 금지) | A-11 | Accepted |
| [0013](0013-resource-scope-permission-pattern.md) | resource:scope Permission 패턴 | security.md | Accepted |
| [0014](0014-no-framework-annotations-in-domain.md) | Domain에 프레임워크 어노테이션 금지 | D-2 | Accepted |
| [0015](0015-no-logging-framework-in-domain.md) | Domain에 로깅 프레임워크 금지 | D-3 | Accepted |

## ADR 작성 가이드

새 ADR은 다음 규칙을 따른다:
- 파일명: `{번호}-{kebab-case-제목}.md`
- 형식: MADR 4.0 (`## Status`, `## Context`, `## Decision`, `## Consequences`)
- 번호: 순차 증가. 기존 ADR 번호 재사용 금지.
- 상태: `Proposed` → `Accepted` 또는 `Rejected`

**신규 ADR 트리거 조건**:
- 업계 표준과 다른 결정을 내릴 때
- `.claude/rules/` 파일의 MUST/MUST NOT 규칙을 새로 추가하거나 변경할 때
- 기술 스택 변경 (라이브러리 교체, 프레임워크 업그레이드)
- 의존성 방향이나 모듈 구조 변경
