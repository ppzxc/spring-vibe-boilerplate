# Architecture Decision Records

이 디렉토리는 프로젝트의 주요 아키텍처 결정(ADR)을 기록한다.
형식은 [MADR 4.0](https://adr.github.io/madr/)을 따른다.

## 인덱스

| ADR | 상태 | 주제 |
|-----|------|------|
| [ADR-0001](0001-hexagonal-architecture-and-cqrs.md) | accepted | Hexagonal Architecture + CQRS 경계 분리 채택 |
| [ADR-0002](0002-flat-module-structure.md) | accepted | 모듈 레이아웃: boilerplate/ 하위 플랫 구조 |
| [ADR-0003](0003-package-structure-and-naming.md) | accepted | 패키지 구조 및 네이밍 컨벤션 |
| [ADR-0004](0004-architecture-testing-strategy.md) | accepted | 아키텍처 테스트 전략: ArchUnit |
| [ADR-0005](0005-code-quality-toolchain.md) | accepted | 코드 품질 도구 전략: Spotless + Checkstyle + ErrorProne + NullAway |
| [ADR-0006](0006-ci-pipeline-strategy.md) | accepted | CI 파이프라인 전략: Lefthook + GitHub Actions + JaCoCo + OpenRewrite |
| [ADR-0007](0007-error-handling-strategy.md) | accepted | 에러 처리 전략: ProblemDetail (RFC 9457) + 커스텀 ErrorCode enum |
| [ADR-0008](0008-observability-strategy.md) | accepted | 관측성 전략: 구조화 로깅 + SLF4J fluent API + Actuator + OTel OTLP |
| [ADR-0009](0009-api-documentation-strategy.md) | accepted | API 문서화 전략: springdoc-openapi + Redoc + Springwolf + AsyncAPI |
| [ADR-0010](0010-containerization-strategy.md) | accepted | 컨테이너화 전략: bootBuildImage + 멀티스테이지 Dockerfile + docker-compose |
| [ADR-0011](0011-configuration-strategy.md) | accepted | 환경 설정 전략: application-{profile}.yml 분리 + 환경변수 오버라이드 패턴 |
| [ADR-0012](0012-transaction-management-strategy.md) | accepted | 트랜잭션 관리 전략: AutoConfiguration 데코레이터 패턴 |
| [ADR-0013](0013-object-mapping-strategy.md) | accepted | 객체 변환 전략: 하이브리드 (MapStruct + static factory) |
| [ADR-0014](0014-module-autoconfiguration-assembly-strategy.md) | accepted | 모듈 자동 조립 전략: 모듈별 AutoConfiguration 자체 등록 |
| [ADR-0015](0015-multi-tenancy-strategy.md) | accepted | 멀티테넌시 전략: Row-level 테넌트 격리 (PostgreSQL RLS 기반) |
| [ADR-0016](0016-authentication-strategy.md) | accepted | 인증 전략: 시나리오별 가이드 (Resource Server / Authorization Server / Custom Starter) |
| [ADR-0017](0017-error-model-aip193-problemdetail.md) | accepted | 에러 모델 전략: AIP-193 에러 코드 + RFC 9457 ProblemDetail |
| [ADR-0018](0018-persistence-technology-selection-guide.md) | accepted | 영속화 기술 선택 가이드: DB + ORM + Migration 조합 |
| [ADR-0019](0019-cache-strategy.md) | accepted | 캐시 전략: Caffeine 기본 + Redis 확장 경로 |
| [ADR-0020](0020-database-migration-strategy.md) | accepted | DB 마이그레이션 전략: Flyway 채택 |
| [ADR-0021](0021-resilience-strategy.md) | accepted | 외부 서비스 내결함성 전략: Resilience4j core + 프로그래매틱 API |
| [ADR-0022](0022-contract-testing-strategy.md) | accepted | Spring Cloud Contract로 API 계약 테스트 도입 |
| [ADR-0023](0023-mutation-testing-strategy.md) | accepted | Mutation Testing으로 테스트 품질 검증 |
| [ADR-0024](0024-virtual-threads-hikari-pool-sizing.md) | accepted | Virtual Threads 환경 HikariCP Pool Size 결정 기준 |
| [ADR-0025](0025-stub-module-retention-strategy.md) | accepted | 스텁 모듈(ws, external) 유지 및 예외 변환 패턴 추가 결정 |

## 언제 ADR을 써야 하는가

ADR은 **되돌리기 어렵거나 팀 전체에 영향을 미치는 기술 결정**에 작성한다.

| 작성 O | 작성 X |
|--------|--------|
| 기술 스택 선택 (DB, 프레임워크, 언어) | 특정 클래스 구현 방식 |
| 아키텍처 패턴 채택 (헥사고날, CQRS 등) | 변수명, 메서드명 선택 |
| 팀 규칙 변경 (코드 포맷, 테스트 전략) | 일반적인 버그 수정 |
| 외부 서비스 연동 방식 결정 | 리팩토링 (동일 패턴 유지 시) |
| 보안/규정 준수 결정 | 기존 ADR로 이미 커버되는 결정 |

**판단 기준:** "6개월 후 이 결정을 왜 했는지 기억할 수 없을 것 같다" → ADR 작성.

### ADR 작성 프로세스

1. **발견** — 팀이 결정해야 할 아키텍처 문제를 인식한다
2. **논의** — 2개 이상의 옵션을 비교하고 트레이드오프를 분석한다
3. **결정** — 결정 참여자가 합의에 도달한다
4. **기록** — `.claude/rules/rules-maintenance.md` 형식으로 ADR을 작성한다
5. **규칙 동기화** — 결정에서 나온 제약을 `.claude/rules/` 파일에 추가한다

## 새 ADR 추가

`.claude/rules/rules-maintenance.md` 의 MADR 작성 형식을 따른다.
번호는 현재 최대값 + 1, 파일명은 `NNNN-<kebab-case-title>.md`.
