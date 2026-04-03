---
status: accepted
date: 2026-04-03
decision-makers: ppzxc
---

# 멀티테넌시 전략: Row-level 테넌트 격리 (PostgreSQL RLS 기반)

## Context and Problem Statement

SaaS 제품을 운영할 때 하나의 애플리케이션 인스턴스가 여러 테넌트(고객 조직)의 데이터를 처리한다.
테넌트 간 데이터 누출을 방지하면서 운영 비용을 합리적으로 유지하는 격리 전략이 필요하다.
업계에서 사용되는 전략은 Row-level, Schema-per-Tenant, Database-per-Tenant 세 가지이며,
초기에 어떤 전략을 기본으로 삼을지와 향후 단계적 격리 강화 경로를 결정해야 한다.

## Decision Drivers

* 초기 운영 비용 최소화 — DB 연결 수, 마이그레이션 복잡도 낮게 유지
* 개발자 실수로 인한 테넌트 데이터 누출 방지 — 애플리케이션 레이어가 아닌 DB 레벨 강제
* Hexagonal Architecture 준수 — 테넌트 식별은 adapter 관심사, domain/application은 테넌트를 모름
* 향후 격리 강화 경로 확보 — Row-level → Schema → DB-per-Tenant 단계적 이동 가능
* Virtual Thread 환경 호환 — `ThreadLocal` 금지(ADR-0005), `ScopedValue` 기반 컨텍스트 전파
* ORM/쿼리 도구 변경 시에도 테넌트 필터 보장 — Native SQL 실수로 인한 누출 방지

## Considered Options

* Option 1: Row-level + PostgreSQL RLS (채택)
* Option 2: Schema-per-Tenant
* Option 3: Database-per-Tenant

## Decision Outcome

Chosen option: "Option 1: Row-level + PostgreSQL RLS", because 운영 비용을 최소화하면서 DB 레벨에서 격리를 강제하여 개발자 실수로 인한 테넌트 데이터 누출을 방지한다. 격리 요구가 커질 경우 Schema-per-Tenant 또는 Database-per-Tenant로 단계적으로 이동하는 경로를 가이드라인으로 유지한다.

### Consequences

* Good, because DB 1개로 수천 테넌트를 운영할 수 있어 초기 인프라 비용이 낮다
* Good, because PostgreSQL RLS가 쿼리 레벨에서 `tenant_id` 필터를 강제하여 애플리케이션 버그로 인한 데이터 누출을 DB가 차단한다
* Good, because `ScopedValue` 기반 `TenantContext`로 Virtual Thread 환경과 호환된다
* Good, because domain/application 레이어는 테넌트를 인식하지 않아 헥사고날 아키텍처 경계를 유지한다
* Bad, because 모든 테이블에 `tenant_id` 컬럼이 필요하여 스키마가 복잡해진다
* Bad, because RLS 정책 설정 실수 시 전체 테넌트 데이터가 노출될 수 있어 DB 마이그레이션 신중 필요
* Bad, because 대형 테넌트가 같은 DB를 공유하므로 "noisy neighbor" 문제가 발생할 수 있다
* Bad, because Native Query(`nativeQuery=true`, JDBC 직접 호출)에는 Hibernate Filter가 적용되지 않아 `tenant_id` 필터를 수동으로 포함해야 하며 누락 시 데이터 누출

### Confirmation

* 모든 테이블 `tenant_id` 컬럼 존재 여부를 DB 마이그레이션 스크립트에서 검증할 것
* `TenantResolutionFilter`가 요청마다 `TenantContext`를 설정하는지 통합 테스트로 확인
* RLS 정책이 활성화된 상태에서 테넌트 A의 자격증명으로 테넌트 B의 데이터에 접근 시도 시 빈 결과 반환 확인

## ORM/Query Tool Defense Layers

RLS는 DB 레벨 1차 방어선이다. ORM/쿼리 도구는 애플리케이션 레벨 2차 방어선으로 함께 적용한다.

| 도구 | 자동 필터링 메커니즘 | Native SQL 보호 | 안전도 |
|------|-------------------|----------------|--------|
| PostgreSQL RLS | 세션 변수 기반 DB 자동 필터 | O (DB 레벨) | ★★★★★ |
| jOOQ `Policy` / `VisitListener` | DSL 렌더링 시점 AST 주입 | O (DSL 쿼리 한정) | ★★★★★ |
| Hibernate `@FilterDef` + `@Filter` | 세션 활성화 시 JPQL 자동 주입 | X (Native SQL 미적용) | ★★★☆☆ |
| EclipseLink `@AdditionalCriteria` | 엔티티 단위 자동 필터 | X | ★★★☆☆ |
| MyBatis Interceptor | SQL 파싱 후 문자열 주입 | 불안정 | ★★☆☆☆ |
| Spring Data JDBC / JdbcTemplate | 자동 메커니즘 없음 — 수동 `WHERE` | X | ★☆☆☆☆ |
| JDBC 직접 호출 | 없음 — 전적으로 개발자 의존 | X | ★☆☆☆☆ |

### 이중 방어 원칙

- **1차 방어**: PostgreSQL RLS — DB가 세션 변수로 격리 강제
- **2차 방어**: ORM/쿼리 도구 레벨 필터 — Hibernate `@Filter` 또는 jOOQ `Policy`
- **Native SQL 구멍**: Hibernate `@Filter`는 `nativeQuery=true`, JDBC 직접 호출에 적용되지 않음. 반드시 `WHERE tenant_id = :tenantId` 수동 포함 및 코드 리뷰 필수
- **RLS 미지원 DB 사용 시**: Hibernate `@Filter` 또는 jOOQ `VisitListener`가 유일한 방어선이 됨. 반드시 신규 ADR 작성 후 ORM 레벨 필터를 강제할 것

## Pros and Cons of the Options

### Option 1: Row-level + PostgreSQL RLS (채택)

모든 테이블에 `tenant_id` 컬럼 추가. PostgreSQL Row Security Policy로 DB 레벨 격리 강제.
애플리케이션에서 세션 변수(`SET app.tenant_id = ?`) 설정 후 RLS가 자동 필터링.

* Good, because DB가 격리를 보장하므로 애플리케이션 코드 버그의 영향 범위를 제한한다
* Good, because 단일 DB 인스턴스로 운영 복잡도를 낮춘다
* Good, because Salesforce, Notion, Linear 등 대형 SaaS의 검증된 패턴이다
* Neutral, because 특정 테넌트의 데이터 전체 삭제/내보내기가 `WHERE tenant_id = ?` 한 쿼리로 가능하다
* Bad, because 대형 테넌트와 소형 테넌트가 동일 DB를 공유하여 성능 격리가 어렵다
* Bad, because RLS를 지원하지 않는 DB로 교체 시 재설계 필요

### Option 2: Schema-per-Tenant

동일 DB 인스턴스 내 테넌트별 스키마 분리. Hibernate `MultiTenancyStrategy.SCHEMA` 사용.

* Good, because 테넌트 데이터가 스키마 수준에서 분리되어 실수로 인한 누출 위험이 낮다
* Good, because 특정 테넌트 스키마만 백업/복구 가능
* Bad, because 신규 테넌트 추가 시 스키마 생성 + Flyway 마이그레이션을 테넌트별로 실행해야 한다
* Bad, because 수천 테넌트 시 스키마 수만큼 DB 오브젝트가 폭증하여 PostgreSQL 성능 저하

### Option 3: Database-per-Tenant

테넌트마다 독립 DB 인스턴스. `AbstractRoutingDataSource`로 동적 DataSource 선택.

* Good, because 완전한 데이터 격리로 규제(GDPR, HIPAA) 대응이 가장 용이하다
* Good, because Shopify의 Pod 아키텍처처럼 테넌트 단위 성능 보장 가능
* Bad, because DB 인스턴스 수만큼 운영 비용과 복잡도가 선형 증가
* Bad, because 연결 풀이 테넌트별로 분리되어 전체 DB 연결 수가 폭증한다

## 단계적 격리 강화 가이드라인

### Row-level → Schema-per-Tenant

**트리거**: 특정 Enterprise 고객이 스키마 격리를 계약 조건으로 요구하거나, 테넌트 수가 수백 개 이하로 유지될 것으로 예상될 때.

**마이그레이션 경로**:
1. `CurrentTenantIdentifierResolver` + `MultiTenantConnectionProvider` 구현 (Hibernate)
2. 대상 테넌트 스키마 생성 + 기존 `tenant_id` 기반 데이터 복사
3. RLS 정책 제거 후 스키마 경로 기반 격리로 전환
4. 신규 테넌트는 스키마 생성 자동화 파이프라인으로 처리

**참고**: JHipster의 `TenantService` + Flyway 다중 스키마 패턴.

### Schema-per-Tenant → Database-per-Tenant

**트리거**: 규제 요구(데이터 주권, HIPAA BAA 등), 특정 테넌트의 부하가 다른 테넌트에 영향을 줄 때.

**마이그레이션 경로**:
1. `AbstractRoutingDataSource` 확장 + DataSource 레지스트리 구현
2. 테넌트별 DataSource를 동적으로 생성/등록하는 `TenantDataSourceManager` 구현
3. 스키마 데이터를 신규 DB로 이전 후 라우팅 전환
4. Shopify Pod 아키텍처처럼 테넌트 그룹(Pod) 단위로 DB 분리 고려

## More Information

* 헥사고날 아키텍처 레이어 경계: [ADR-0001](0001-hexagonal-architecture-and-cqrs.md)
* Virtual Thread `ThreadLocal` 금지 근거: [ADR-0005](0005-code-quality-toolchain.md)
* 규칙 파일: `.claude/rules/multi-tenancy.md`
* PostgreSQL RLS 공식 문서: https://www.postgresql.org/docs/current/ddl-rowsecurity.html
* Hibernate Multi-tenancy 공식 문서: https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html#multitenancy
