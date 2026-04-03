---
status: accepted
date: 2026-04-03
decision-makers: ppzxc
---

# 영속화 기술 선택 가이드: DB + ORM + Migration 조합

## Context and Problem Statement

스켈레톤이 특정 DB/ORM/Migration 도구를 강제하면 프로젝트 요구사항과 맞지 않을 때 대규모 교체가 필요하다.
업계 표준과 리더 기업의 사용 사례를 기반으로 선택 가이드를 제공하여,
프로젝트 시작 시 Claude Code가 적합한 기술 조합을 생성하도록 한다.

## Decision Drivers

* 범용성 — 다양한 영속화 기술 조합 대응
* 업계 표준 기반의 객관적 선택 기준 제공
* ADR-0015 멀티테넌시(PostgreSQL RLS) 호환성 고려
* 프로젝트별 유연성 확보

## Considered Options

1. PostgreSQL + JPA + Flyway를 기본값으로 고정하고 코드 포함
2. 선택 가이드만 제공 (코드 미포함)
3. 복수 프로파일로 여러 조합을 동시 제공

## Decision Outcome

Chosen option: "선택 가이드만 제공 (코드 미포함)", because 프로젝트마다 최적의 DB/ORM/Migration 조합이 다르며,
Claude Code가 이 가이드를 읽고 프로젝트 시작 시 적합한 코드를 생성한다.

### Consequences

* Good, because 어떤 DB/ORM 조합이든 스켈레톤에서 시작 가능
* Good, because 불필요한 의존성을 제거할 필요가 없음
* Bad, because 첫 프로젝트 설정 시 영속화 코드를 직접 생성해야 함

## DB 선택지

| DB | 표준 여부 | 업계 리더 사용 사례 | 적합한 경우 |
|----|----------|-------------------|------------|
| PostgreSQL | RDBMS de facto 표준 | Netflix, Spotify, Uber, Instagram, GitLab | ACID 트랜잭션, JSON 지원, RLS (ADR-0015 전제) |
| MySQL | 가장 높은 점유율 | Meta, X(Twitter), GitHub, Shopify | 읽기 중심 워크로드, 레거시 호환 |
| MongoDB | NoSQL 리더 | Uber(부분), eBay | 비정형 데이터, 빠른 프로토타이핑, 유연한 스키마 |

## ORM / Data Access 선택지

| 도구 | 표준 여부 | 업계 리더 사용 사례 | 적합한 경우 |
|------|----------|-------------------|------------|
| Spring Data JPA (Hibernate) | Java ORM 사실상 표준 | 대부분의 Spring Boot 프로젝트 | 도메인 중심 설계, 복잡한 객체 관계, 자동 DDL |
| jOOQ | SQL 중심 de facto 표준 | Scalable Capital, Swiss RE | 복잡한 쿼리, SQL 제어 필요, 컴파일 타임 타입 안전성 |
| MyBatis | 아시아권 높은 채택률 | NHN, LINE, 쿠팡(레거시) | 복잡한 SQL 직접 제어, 레거시 DB 연동 |
| Spring Data R2DBC | 리액티브 표준 | 리액티브 스택 프로젝트 | 논블로킹 I/O, WebFlux 조합 (이 스켈레톤은 MVC이므로 비추천) |

## Migration 도구 선택지

| 도구 | 표준 여부 | 업계 리더 사용 사례 | 적합한 경우 |
|------|----------|-------------------|------------|
| Flyway | Spring Boot RDBMS de facto 표준 | Netflix, Spotify, Axon Framework | SQL 중심, RDBMS, 낮은 학습곡선 |
| Liquibase | 엔터프라이즈 표준 | JHipster, 엔터프라이즈 환경 | 다중 DB 벤더, rollback, diff 자동 생성 |
| Mongock | MongoDB 마이그레이션 표준 | MongoDB 기반 프로젝트 | MongoDB 선택 시 |

## 추천 조합

| 시나리오 | DB | ORM | Migration | 비고 |
|----------|-----|-----|-----------|------|
| 범용 CRUD API | PostgreSQL | Spring Data JPA | Flyway | 가장 넓은 레퍼런스, ADR-0015 RLS 호환 |
| 복잡 쿼리 중심 | PostgreSQL | jOOQ | Flyway | SQL 제어 + 타입 안전성 |
| 레거시 연동 | MySQL / Oracle | MyBatis | Liquibase | SQL 직접 제어 + 다중 DB 벤더 |
| 비정형 데이터 | MongoDB | Spring Data MongoDB | Mongock | 스키마 유연성 |

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| PostgreSQL + JPA + Flyway 고정 | MongoDB/jOOQ/MyBatis 프로젝트에서 대규모 교체 필요 |
| 복수 프로파일 동시 제공 | 빌드 복잡도 증가, 사용하지 않는 의존성 잔류 |

## More Information

→ [ADR-0015](0015-multi-tenancy-strategy.md) — 멀티테넌시 전략 (PostgreSQL RLS)
→ [ADR-0002](0002-flat-module-structure.md) — 모듈 레이아웃
→ [module-add.md](../../.claude/rules/module-add.md) — 새 모듈 추가 절차
