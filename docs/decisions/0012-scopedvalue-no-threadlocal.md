# ADR-0012: ScopedValue (ThreadLocal 금지)

## Status

Accepted

## Context

Spring Boot 4에서 Virtual Thread가 기본 활성화된다 (`spring.threads.virtual.enabled=true`). 비즈니스 컨텍스트(userId, tenantId)를 요청 스코프 내에서 전파해야 하는데, ThreadLocal은 Virtual Thread 환경에서 각 가상 스레드마다 새 복사본을 생성하여 전파가 불안정하다.

## Decision

비즈니스 컨텍스트(userId, tenantId, permissions) 전파에 **Java 25 ScopedValue**를 사용한다.

- `ThreadLocal` 사용 금지 (A-11)
- Spring Security의 `SecurityContextHolder`(ThreadLocal)는 그대로 유지 (Spring 공식 미지원)
- `traceId`/`spanId`는 OpenTelemetry가 MDC에 자동 주입 → ScopedValue 불필요
- 비구조적 비동기(`CompletableFuture`, `@Async`)에서 ScopedValue 사용 금지 (바인딩 유실)

## Consequences

Virtual Thread 환경에서 안전한 컨텍스트 전파. Java 25 표준 API 사용. StructuredTaskScope 자식 스레드에 자동 상속. 비구조적 비동기 사용 불가 제약이 생김.
