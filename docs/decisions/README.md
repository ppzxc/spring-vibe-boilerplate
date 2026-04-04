# Architecture Decision Records

이 디렉토리는 프로젝트의 주요 아키텍처 결정(ADR)을 기록한다.
형식은 [MADR 4.0](https://adr.github.io/madr/)을 따른다.

## 인덱스

| ADR | 상태 | 주제 |
|-----|------|------|
| [ADR-0001](0001-hexagonal-architecture-and-cqrs.md) | accepted | Hexagonal Architecture + CQRS 경계 분리 채택 |
| [ADR-0002](0002-flat-module-structure.md) | accepted | 모듈 레이아웃: template/ 하위 플랫 구조 |
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

## 새 ADR 추가

`.claude/rules/rules-maintenance.md` 의 MADR 작성 형식을 따른다.
번호는 현재 최대값 + 1, 파일명은 `NNNN-<kebab-case-title>.md`.
