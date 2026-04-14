# ADR-0004: CQRS Level 1 — 클래스 분리, 동일 DB

## Status

Accepted

## Context

Command와 Query가 같은 Service 클래스에 혼재되면 Command 로직 변경이 Query에 영향을 주고, 반대도 마찬가지다. Read 성능 최적화(DTO 직접 프로젝션)와 Write 정합성(Aggregate 경유)을 분리하고 싶다.

그러나 별도 Read Model/DB를 도입하면 Event Sourcing이나 CDC 파이프라인 인프라가 필요해져 초기 복잡도가 과도하다.

## Decision

**Level 1 CQRS**를 적용한다. Command/Query 클래스 분리, 동일 DB.

| Level | 설명 | 적용 |
|-------|------|------|
| **Level 1** | Command/Query 클래스 분리, 동일 DB | 기본 적용 |
| Level 2 | 별도 Read Model/Store | 성능 요구 시 확장 |
| Level 3 | Event Sourcing | 범위 외 |

**Output Port 3분할** (A-3):
- `Load{Subject}Port`: Domain 객체 로드, `Optional` 반환 강제
- `Save{Subject}Port`: Aggregate 저장 + 이벤트 수거
- `{Subject}QueryPort`: DTO 직접 프로젝션, Aggregate 비경유

**1 TX = 1 Aggregate** (A-9): 하나의 UseCase에서 복수 Aggregate 동일 TX 변경 금지. 복수 Aggregate 변경은 이벤트 기반으로 분리.

**UseCase Bypass 금지**: Controller에서 QueryPort를 직접 호출 금지. 단순 위임도 UseCase 경유.

**Level 2 확장**: QueryPort 구현체를 Cache, Read-Replica, 또는 별도 Projection Store로 교체. Port 인터페이스는 그대로 유지.

## Consequences

### Positive
- Query Side: DB → DTO 직접 프로젝션으로 N+1, 불필요한 Domain 로드 없음
- Command Side: Aggregate 경유로 비즈니스 불변식 보장
- Level 2 확장 시 Port 인터페이스 변경 없이 구현체만 교체

### Negative
- 단순 조회도 UseCase 인터페이스 + Service 필요 → 파일 수 증가
- 1 TX = 1 Aggregate 제약으로 분산 트랜잭션을 이벤트로 처리해야 함
