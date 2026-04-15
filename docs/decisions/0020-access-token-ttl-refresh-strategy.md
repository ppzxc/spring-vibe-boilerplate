# ADR-0020: Access Token TTL + Refresh Token 전략

## Status

Accepted

## Context

IDP(Identity Provider)가 발급한 JWT Access Token을 Resource Server가 검증할 때, 권한 변경이 즉시 반영되어야 하는지(실시간성)와 성능(매 요청마다 IDP 조회 vs 캐시)을 균형잡아야 한다.

선택지:
- **긴 TTL (1시간+)**: 권한 변경 지연 최대 1시간, 탈취 위험 높음.
- **짧은 TTL (5~15분) + Refresh Token** (채택): 권한 변경 최대 반영 지연 = TTL, 탈취 피해 최소화.
- **Opaque Token + Introspection**: 실시간 권한 반영, 매 요청마다 IDP 네트워크 호출.

Modulith 단계에서는 in-process 이벤트로 내부 BC 간 권한 동기화가 가능하지만, 외부 Resource Server는 다른 JVM이므로 Spring Modulith 이벤트가 닿지 않는다.

## Decision

짧은 Access Token TTL(5~15분) + Refresh Token 순환 전략을 기본으로 채택한다.

- Access Token TTL: 5~15분 (운영 환경별 조정)
- Refresh Token: 장기 유효, DB 저장, 회전(rotation) 방식
- 긴급 차단: Token Revocation → IDP Revocation List + /introspect 연동

단계별 확장 전략:
1. 초기: 짧은 TTL + Revocation (코드 변경 없음)
2. 성장기: Redis Pub/Sub으로 권한 변경 이벤트 무효화
3. 확장기: Kafka + OpenID CAEP 표준 이벤트

## Consequences

- ✅ 권한 변경 최대 지연 = Access Token TTL (5~15분) — 비즈니스 수용 가능
- ✅ 탈취된 Access Token의 유효 기간 최소화
- ✅ Resource Server가 매 요청 IDP 호출 불필요 → 성능 유지
- ✅ Refresh Token 회전으로 재사용 탐지 가능
- ⚠️ 권한 즉시 박탈이 필요한 경우 → Revocation 엔드포인트 필수 구현
- ⚠️ Refresh Token 저장소(DB) 관리 필요
