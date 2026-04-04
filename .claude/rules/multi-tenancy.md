# Multi-tenancy Rules

## 기본 전략: Row-level + PostgreSQL RLS [ADR-0015]

- 테넌트 격리 전략은 Row-level (PostgreSQL RLS 기반)을 기본으로 사용할 것 [ADR-0015]
- 스키마 분리(Schema-per-Tenant) 또는 DB 분리(Database-per-Tenant)로 전환이 필요한 경우 ADR-0015 가이드라인을 따를 것

## 레이어 경계 규칙 [ADR-0015]

- domain/application 레이어는 테넌트를 인식하지 않을 것 — 테넌트 식별은 adapter 관심사
- 테넌트 컨텍스트는 `ScopedValue`로 전파할 것 (`ThreadLocal` 금지 — ADR-0005)
- 테넌트 식별 필터(`TenantResolutionFilter`)는 `boilerplate-adapter-input-api`에 배치할 것

## 스키마 설계 규칙 [ADR-0015]

- 테넌트 데이터를 저장하는 모든 테이블에 `tenant_id` 컬럼을 포함할 것
- PostgreSQL RLS 정책은 DB 마이그레이션(Flyway/Liquibase) 스크립트로 관리할 것
- 애플리케이션에서 RLS 활성화를 위해 세션 변수(`SET app.tenant_id`)를 설정할 것 — 쿼리마다 `WHERE tenant_id = ?` 수동 추가 금지

## 테넌트 식별 규칙 [ADR-0015]

- 테넌트 식별 소스는 하나만 사용할 것 (HTTP 헤더 `X-Tenant-ID` 또는 JWT 클레임 또는 서브도메인 중 선택)
- 테넌트 식별에 실패한 요청은 인증 오류(401/403)로 거부할 것 — 기본 테넌트 폴백 금지

## 쿼리 도구별 테넌트 필터 규칙 [ADR-0015]

- PostgreSQL RLS(1차)와 ORM 레벨 필터(2차)를 함께 적용할 것 — 이중 방어
- Hibernate 사용 시 `@FilterDef` + `@Filter`를 엔티티에 선언하고 세션마다 활성화할 것
- jOOQ 사용 시 `Policy` (3.17+) 또는 `VisitListener`로 모든 DSL 쿼리에 자동 주입할 것
- Native SQL(`nativeQuery=true`, JDBC 직접 호출)에는 Hibernate Filter가 적용되지 않음 — `WHERE tenant_id = ?` 수동 포함 필수, 코드 리뷰 강제
- RLS 미지원 DB(MySQL, H2 등) 도입 시 ORM 레벨 필터가 유일한 방어선 — 신규 ADR 작성 후 적용할 것

## 단계적 격리 강화 [ADR-0015]

- Row-level → Schema-per-Tenant 전환: Enterprise 고객 요구 또는 계약 조건 발생 시
- Schema-per-Tenant → Database-per-Tenant 전환: 규제 요구(GDPR, HIPAA) 또는 noisy neighbor 문제 발생 시
- 각 전환 경로는 ADR-0015 가이드라인 참조
