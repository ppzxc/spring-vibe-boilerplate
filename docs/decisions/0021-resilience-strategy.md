---
status: accepted
date: 2026-04-04
decision-makers: ppzxc
---

# 외부 서비스 내결함성 전략: Resilience4j core 라이브러리 + 프로그래매틱 API

## Context and Problem Statement

외부 API를 호출하는 Outbound Adapter에서 CircuitBreaker, Retry, RateLimiter 패턴을 적용해야 한다.
`resilience4j-spring-boot3` starter는 어노테이션 기반 AOP를 제공하지만 Spring Boot 4 호환성이 미확인이며,
이 프로젝트는 adapter 모듈에서 `@Component`/`@Service` 스테레오타입을 금지하고 AutoConfiguration으로 Bean을 등록하는 패턴을 사용한다.

## Decision Outcome

Chosen option: "Resilience4j core 라이브러리 + 프로그래매틱 API (`Decorators.ofSupplier(...)`)", because Spring Boot 4 호환성이 검증된 core 모듈만 사용하면 AOP 의존 없이 어댑터 클래스 내부에서 직접 조합할 수 있고, 프로젝트의 수동 Bean 등록 패턴(`AutoConfiguration + @Bean`)과 일관성을 유지할 수 있다.

## 미채택 옵션

| 옵션 | 미채택 이유 |
|------|-----------|
| `resilience4j-spring-boot3` starter | Spring Boot 4 호환 미확인, AOP + 어노테이션 의존으로 프로젝트 규칙(`@Component` 금지)과 충돌 가능 |
| Spring Retry | CircuitBreaker, RateLimiter 미지원 — 단순 재시도만 커버 |

## 적용 패턴

- CircuitBreaker + Retry + RateLimiter 세 가지 패턴을 `Decorators` API로 조합
- 각 레지스트리(`CircuitBreakerRegistry`, `RetryRegistry`, `RateLimiterRegistry`)는 `ExternalAdapterAutoConfiguration`에서 `@ConditionalOnMissingBean`으로 등록 — 프로젝트별 커스텀 설정 교체 가능
- Micrometer 연동(`resilience4j-micrometer`)으로 Circuit 상태 메트릭 노출
