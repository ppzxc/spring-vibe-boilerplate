# ADR-0011: UUIDv7 강제

## Status

Accepted

## Context

Aggregate Root 식별자로 UUID를 사용할 때 UUIDv4(`UUID.randomUUID()`)는 완전 랜덤으로 B-Tree 인덱스 페이지 분할을 일으켜 삽입 성능이 저하된다. Auto Increment는 분산 환경에서 충돌 위험이 있다.

## Decision

Aggregate Root 식별자는 **UUIDv7** (RFC 9562 §5.7)을 사용한다.

- `UUID.randomUUID()` (UUIDv4) 금지
- Auto Increment 금지
- 외부 라이브러리(`uuid-creator` 등) 금지 — Domain 순수성 원칙(D-1) 위반

구현: **순수 Java**, `AtomicReference<State>` CAS + `ThreadLocalRandom` 조합.

설계 근거:
- `AtomicReference<State>`: timestamp + counter 원자적 갱신 (race condition 방지)
- `ThreadLocalRandom`: Virtual Thread 친화적, SecureRandom 초기화 오버헤드 없음
- monotonic counter (0~4095): 동일 ms 내 순서 보장, overflow 시 next ms 대기
- clock rollback 처리: ts <= prev.timestamp → counter 증가 (B-Tree 정렬 역행 방지)

UUIDv7은 시간 순서로 정렬되어 B-Tree 삽입 성능이 UUIDv4 대비 크게 향상된다.

상세 구현 → `.claude/rules/scaffold.md §식별자 전략 (UUIDv7)` 참조

## Consequences

인덱스 순차 삽입으로 DB 쓰기 성능 향상. 시간 기반 정렬이 필요한 쿼리에서 UUID 자체로 정렬 가능. `UUID.randomUUID()` 호출을 ArchUnit으로 감지하여 위반 방지.
