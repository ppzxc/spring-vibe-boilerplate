---
status: accepted
date: 2026-04-03
decision-makers: ppzxc
---

# 에러 모델 전략: AIP-193 에러 코드 + RFC 9457 ProblemDetail

## Context and Problem Statement

ADR-0007에서 ProblemDetail(RFC 9457) + 커스텀 ErrorCode enum 조합을 결정했으나,
에러 코드 체계의 구체적 표준이 미정이었다.
gRPC 확장 가능성(build.gradle.kts에 proto 라벨 존재)을 고려하면
REST와 gRPC 간 일관된 에러 처리를 위해 업계 표준 에러 코드 체계를 채택해야 한다.

## Decision Drivers

* gRPC canonical status codes와의 호환성
* Google, Microsoft 등 업계 리더가 검증한 에러 체계
* 클라이언트 SDK 자동 생성 시 일관된 에러 처리 지원
* REST API에서도 ProblemDetail 표준을 유지

## Considered Options

1. AIP-193 에러 코드 + ProblemDetail
2. 자체 정의 에러 코드 + ProblemDetail
3. Microsoft REST API Guidelines 에러 모델

## Decision Outcome

Chosen option: "AIP-193 + ProblemDetail", because Google이 전 API(Cloud, YouTube, Maps)에 적용 중인
검증된 체계이고, gRPC canonical status codes와 1:1 매핑되어 REST ↔ gRPC 확장 시
일관된 에러 처리가 가능하다.

### Consequences

* Good, because REST와 gRPC에서 동일한 에러 코드 체계 사용 가능
* Good, because AIP-193은 Google이 대규모로 검증한 업계 표준
* Good, because ProblemDetail은 RFC 9457 표준이며 Spring Boot 4가 네이티브 지원
* Bad, because AIP-193 코드만으로 표현 불가능한 비즈니스 에러는 커스텀 확장 필요

## ErrorCode ↔ HTTP ↔ gRPC 매핑

| ErrorCode | HTTP | gRPC Status | 설명 |
|-----------|------|-------------|------|
| INVALID_ARGUMENT | 400 | INVALID_ARGUMENT | 클라이언트 입력 오류 |
| FAILED_PRECONDITION | 400 | FAILED_PRECONDITION | 사전 조건 미충족 |
| OUT_OF_RANGE | 400 | OUT_OF_RANGE | 값이 허용 범위 초과 |
| UNAUTHENTICATED | 401 | UNAUTHENTICATED | 인증 필요 |
| PERMISSION_DENIED | 403 | PERMISSION_DENIED | 권한 부족 |
| NOT_FOUND | 404 | NOT_FOUND | 리소스 미존재 |
| ALREADY_EXISTS | 409 | ALREADY_EXISTS | 리소스 중복 |
| ABORTED | 409 | ABORTED | 동시성 충돌 |
| RESOURCE_EXHAUSTED | 429 | RESOURCE_EXHAUSTED | 할당량 초과 |
| CANCELLED | 499 | CANCELLED | 클라이언트 취소 |
| INTERNAL | 500 | INTERNAL | 서버 내부 오류 |
| DATA_LOSS | 500 | DATA_LOSS | 복구 불가능한 데이터 손실 |
| UNAVAILABLE | 503 | UNAVAILABLE | 서비스 일시 불가 |
| DEADLINE_EXCEEDED | 504 | DEADLINE_EXCEEDED | 처리 시간 초과 |

## 에러 응답 형식

### 기본 에러 응답

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Order not found",
  "errorCode": "NOT_FOUND",
  "details": []
}
```

### FieldViolation 포함 응답

```json
{
  "type": "about:blank",
  "title": "Invalid Argument",
  "status": 400,
  "detail": "Validation failed",
  "errorCode": "INVALID_ARGUMENT",
  "details": [
    { "field": "email", "description": "must be a valid email" },
    { "field": "age", "description": "must be positive" }
  ]
}
```

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| 자체 정의 에러 코드 | 업계 검증 부재, gRPC 매핑 직접 정의 필요, 유지보수 부담 |
| Microsoft REST API Guidelines | Spring 생태계와의 통합 사례 부족, gRPC 매핑이 AIP보다 불명확 |

## More Information

→ [ADR-0007](0007-error-handling-strategy.md) — ProblemDetail + ErrorCode enum 결정
→ [error-handling.md](../../.claude/rules/error-handling.md) — 에러 처리 규칙
→ [AIP-193](https://google.aip.dev/193) — Google API Improvement Proposals: Errors
