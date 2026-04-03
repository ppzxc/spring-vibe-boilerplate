---
status: accepted
date: 2026-04-04
decision-makers: ppzxc
---

# ADR-0020: DB 마이그레이션 전략 — Flyway 채택

## Context and Problem Statement

`schema.sql`로 스키마를 초기화하면 스키마 버전 관리가 불가능하다. 프로덕션 배포 시 기존 데이터를 유지하면서 스키마를 변경하려면 마이그레이션 체계가 필요하다.

## Considered Options

- **Flyway**: SQL 기반, Spring Boot 네이티브 통합, jOOQ DDLDatabase와 자연스럽게 조합
- **Liquibase**: XML/YAML 포맷, jOOQ SQL-first 접근과 마찰
- **schema.sql 유지**: 버전 관리 불가, 프로덕션 부적합

## Decision Outcome

**Flyway를 채택한다.**

- `spring-boot-starter-flyway` 의존성 사용 (Spring Boot BOM 버전 관리)
- 마이그레이션 파일 위치: `src/main/resources/db/migration/`
- 파일 명명: `VNNNN__description.sql` (Flyway 표준)
- jOOQ DDLDatabase codegen 소스: `db/migration/` 디렉토리 (`sort=flyway` 옵션)
- `BIGINT GENERATED ALWAYS AS IDENTITY`를 표준 identity 컬럼으로 사용

### Consequences

- 신규 스키마 변경은 반드시 Flyway 마이그레이션 스크립트로 추가
- `spring.sql.init.*` 설정 제거
- Testcontainers 통합 테스트에서 Flyway가 자동 실행됨
