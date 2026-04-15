# ADR-0019: BC 기반 인가 배치 원칙

## Status

Accepted

## Context

인가(Authorization) 로직을 어느 계층, 어느 BC에 배치할지 결정해야 한다.

선택지:
1. **모든 BC의 Domain 계층**: Domain 순수성(D-1) 위반. Spring/ScopedValue import 금지 원칙 충돌.
2. **모든 BC의 Application 계층**: Identity BC조차도 Application에서만 인가 처리 → Identity의 Rich Domain Model 약화.
3. **BC 성격에 따른 분리** (채택):
   - Identity BC: Domain 계층 (User, Role, Permission이 비즈니스 도메인 그 자체)
   - 다른 BC: Application 계층 (인가 정책 소비만, AuthorizationPolicy Output Port 경유)

Controller에서 직접 Permission 검사 금지 — 계층 경계 위반 + 테스트 불가.

## Decision

인가 처리 위치는 BC의 성격에 따라 결정한다.

| BC | 인가 위치 | 이유 |
|----|----------|------|
| Identity BC | Domain 계층 | Permission이 비즈니스 도메인 자체 |
| 다른 BC | Application 계층 | 인가 정책 소비자, Domain 순수성 유지 |

2단계 인가:
- 1단계 (Scope): Adapter(Filter)에서 OAuth2 Scope 검사
- 2단계 (Permission): Application에서 AuthorizationPolicy Output Port를 통해 세밀한 Permission 검사

## Consequences

- ✅ Identity BC의 Rich Domain Model 보존 (Permission, Role, User 비즈니스 불변식 Domain 내 캡슐화)
- ✅ 다른 BC의 Domain 순수성 유지 (Spring/ScopedValue import 없음)
- ✅ AuthorizationPolicy Output Port → 테스트 시 Mock 교체 가능
- ✅ 2단계 인가로 Scope(앱 수준) + Permission(사용자 수준) 분리
- ⚠️ BC마다 다른 인가 배치 위치 → 팀 규칙 이해 필요
