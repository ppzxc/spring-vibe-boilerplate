---
status: accepted
date: 2026-04-03
decision-makers: ppzxc
---

# 인증 전략: 시나리오별 가이드 (Resource Server / Authorization Server / Custom Starter)

## Context and Problem Statement

이 스켈레톤은 비즈니스 API 서버, 자체 IdP, IdP 겸 관리 포탈, 조직 내 공유 Security Starter 등
다양한 인증 시나리오에 사용될 수 있다.
하나의 기본값을 강제하면 대부분의 프로젝트에서 코드를 제거하거나 교체해야 하므로,
시나리오별 구현 가이드를 제공하여 프로젝트 시작 시 Claude Code가 적합한 코드를 생성하도록 한다.

## Decision Drivers

* 범용성 — 단일 스켈레톤으로 다양한 인증 시나리오 대응
* IdP-agnostic — 특정 IdP(Keycloak, Auth0, Cognito)에 종속하지 않음
* Spring Security 6+ 공식 지원 패턴 활용
* gRPC 확장 가능성 (proto 라벨이 이미 빌드에 존재)

## Considered Options

1. OAuth2 Resource Server 코드를 스켈레톤에 포함
2. Spring Authorization Server 코드를 스켈레톤에 포함
3. 코드 미포함, 시나리오별 가이드만 제공

## Decision Outcome

Chosen option: "시나리오별 가이드 제공 (코드 미포함)", because 스켈레톤이 다양한 인증 시나리오에 대응해야 하며,
각 시나리오의 의존성과 설정이 크게 달라 하나의 기본값이 오히려 부담이 된다.
Claude Code가 ADR 가이드를 읽고 프로젝트 시작 시 적합한 코드를 생성한다.

### Consequences

* Good, because 어떤 인증 시나리오든 스켈레톤을 그대로 사용 가능
* Good, because clone 후 불필요한 코드를 삭제할 필요가 없음
* Bad, because 첫 프로젝트 설정 시 인증 코드를 직접 생성해야 함

## 시나리오별 구현 가이드

| 시나리오 | 구성 | 사용 사례 | 핵심 의존성 |
|----------|------|----------|------------|
| 비즈니스 API 서버 | OAuth2 Resource Server only | 외부 IdP(Keycloak, Auth0, Cognito)에서 JWT 발급, 이 서버는 검증만 | `spring-boot-starter-oauth2-resource-server` |
| 자체 IdP | Authorization Server only | 토큰 발급 전용 서비스 | `spring-security-oauth2-authorization-server` |
| IdP + 관리 포탈 | Auth Server + Resource Server | 개발자 콘솔, B2B 테넌트 포탈. 자기가 발급한 JWT를 자기가 검증 | 위 두 의존성 모두 |
| 조직 내 공유 | Custom Security Starter | Security 설정을 별도 starter 라이브러리로 추출하여 여러 서비스에서 재사용 | 별도 프로젝트 |

### 시나리오 1: OAuth2 Resource Server (가장 일반적)

Spring Security 6+ 공식 권장 패턴. Netflix, Spotify, Google이 공통으로 사용하는 방식.

설정 포인트:
- `SecurityFilterChain`: stateless 세션 + `oauth2ResourceServer(jwt)` DSL
- `JwtAuthenticationConverter`: JWT claims에서 역할/권한 매핑
- `application.yml`: `spring.security.oauth2.resourceserver.jwt.issuer-uri` 환경변수화
- 로컬 프로파일: 인증 완화 또는 mock JWT 지원

### 시나리오 2: Spring Authorization Server

Spring 공식 IdP 프로젝트. Keycloak의 경량 대안으로, 토큰 발급/관리를 직접 제어해야 할 때 적합.

설정 포인트:
- `AuthorizationServerSettings`: issuer URL, 엔드포인트 경로
- `RegisteredClientRepository`: 클라이언트 등록 관리
- JWK Set 생성 및 로테이션

### 시나리오 3: Authorization Server + Resource Server 합체

자기가 발급한 JWT를 자기가 검증하는 패턴. `issuer-uri`가 자기 자신을 가리킨다.
개발자 콘솔, B2B SaaS 테넌트 관리 포탈에서 사용.

설정 포인트:
- 시나리오 1 + 시나리오 2의 설정을 모두 포함
- `SecurityFilterChain`을 토큰 발급용과 API 보호용으로 분리

### 시나리오 4: Custom Security Starter

조직 규모가 커져 여러 서비스가 동일한 Security 설정을 사용할 때.
Security 설정을 별도 Spring Boot Starter 라이브러리로 추출한다.

설정 포인트:
- 별도 프로젝트로 starter 라이브러리 생성
- `AutoConfiguration`으로 `SecurityFilterChain` 자동 등록
- 서비스는 starter 의존성 추가만으로 인증 설정 완료

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| Resource Server 코드 포함 | IdP/합체 시나리오에서 코드 제거 필요 |
| Authorization Server 코드 포함 | Resource Server 전용 프로젝트에서 불필요한 의존성 |

## More Information

→ [ADR-0001](0001-hexagonal-architecture-and-cqrs.md) — Hexagonal Architecture 원칙
→ [architecture.md](../../.claude/rules/architecture.md) — 레이어 의존성 규칙
