---
status: accepted
date: 2026-04-06
decision-makers: ppzxc
---

# 객체 변환 예외 전략: CQRS 읽기 모델의 Direct Projection 허용

## Context and Problem Statement

ADR-0013(객체 변환 전략)에 의해 어댑터 계층에서 도메인 모델로의 변환 시 MapStruct를 사용하고 있다. 그러나 목록 조회 등 단순 조회(Query) 시에도 모든 영속성 데이터를 도메인 엔티티로 변환한 뒤 API DTO로 다시 변환하는 것은 심각한 성능 저하와 메모리 낭비를 유발한다.

## Decision Drivers

* 읽기 성능 최적화 (N+1 문제 해결, 불필요한 컬럼 조회 방지)
* 도메인 불변성을 훼손하지 않는 범위 내에서의 예외 허용
* CQRS(명령과 조회 책임 분리) 원칙 부합

## Considered Options

* Option 1: 모든 조회도 예외 없이 도메인 모델을 거치도록 강제
* Option 2: 조회의 경우에 한해 영속성 어댑터에서 API 응답 객체(Record)로 Direct Projection 허용

## Decision Outcome

Chosen option: "Option 2: Direct Projection 허용". 
데이터의 상태를 변경하는 Command(Create/Update/Delete)는 반드시 도메인 모델을 통과하여 비즈니스 규칙을 검증해야 하지만, 단순 조회(Query)는 영속성 어댑터에서 데이터베이스 쿼리 결과(예: JdbcTemplate, jOOQ)를 `application` 계층의 읽기 전용 DTO(Record)로 직접 매핑하여 반환하는 것을 허용한다.

### Consequences

* Good, because 읽기 작업 시 불필요한 객체 생성(MapStruct, Domain Entity)을 생략하여 성능이 대폭 향상된다.
* Good, because 복잡한 조인 쿼리 결과를 화면에 맞게 자유롭게 조립할 수 있다.
* Bad, because 읽기와 쓰기의 코드 흐름이 분리되어 학습 곡선이 소폭 상승한다.

## More Information

* 연관 규칙: ADR-0001 (Hexagonal Architecture and CQRS), ADR-0013 (Object Mapping Strategy)