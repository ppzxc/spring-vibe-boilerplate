# Rules Index

이 파일은 항상 로드됩니다. 전체 rules 파일 목록과 로드 조건을 나타냅니다.

> **현재 생성된 파일**: naming.md, scaffold.md, validation.md
> **미생성 파일** (향후 생성 예정): cqrs.md, modulith.md, testing.md, observability.md, security.md, domain.md, application.md, adapter.md
> 미생성 파일과 관련된 작업 시 `docs/decisions/` ADR을 직접 참조하라.

| 파일 | 내용 | 로드 조건 |
|------|------|----------|
| `naming.md` | 클래스/패키지/모듈/파일 네이밍 규칙, 금지 접미사 | 항상 |
| `scaffold.md` | 새 Aggregate/UseCase 생성 템플릿, Inside-Out 워크플로우 | 항상 |
| `validation.md` | 모듈별 금지 import, 컴포넌트별 필수 패턴, 검증 책임 분리 | 항상 |
| `cqrs.md` | CQRS 설계, Command/Query, Output Port 3분할, 이벤트 흐름, Outbox | 항상 |
| `modulith.md` | Gradle 멀티모듈 + Modulith 하이브리드, 빌드 레이블, 이벤트 발행 | 항상 |
| `testing.md` | 테스트 피라미드(70/15/10/5), 계층별 테스트 전략, 5중 방어선 | 항상 |
| `observability.md` | OpenTelemetry, 구조화 로깅, ScopedValue 컨텍스트 전파 | 항상 |
| `security.md` | BC 기반 인가, Permission 모델(resource:scope), JWT 클레임 매핑 | 항상 |
| `domain.md` | Domain 계층 35개 규칙 중 D 규칙 — 순수성, Rich Model, Aggregate | `**/domain/**` |
| `application.md` | Application 계층 규칙 — Port, UseCase, TX 경계, DTO | `**/application/**` |
| `adapter.md` | Adapter 계층 규칙 — 경계, 매핑, jOOQ, 예외 변환, Optimistic Lock | `**/adapter/**` |

## ADR 참조 방식

rules 파일에서 ADR 참조: `> 근거: ADR-NNNN`
ADR 파일 위치: `docs/decisions/NNNN-*.md`
