---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 에러 처리 전략: ProblemDetail (RFC 9457) + 커스텀 ErrorCode enum

## 배경 및 문제

REST API에서 에러 응답 형식이 일관되지 않으면 클라이언트 측 오류 처리가 복잡해진다.
Spring Boot 4는 RFC 9457(Problem Details for HTTP APIs) 표준을 네이티브 지원하지만,
비즈니스 에러 코드를 표준 필드만으로 표현하기 어렵다.
domain/application 레이어에서 HTTP 개념(상태 코드 등)을 직접 사용하면 레이어 경계가 오염된다.

## 결정 기준

* HTTP API 에러 응답의 업계 표준 준수
* domain/application 레이어의 순수 Java 유지
* 클라이언트가 에러 종류를 프로그래밍 방식으로 식별 가능
* i18n 미지원 (보일러플레이트 범위 초과)

## 결정

**ProblemDetail (RFC 9457) + 커스텀 ErrorCode enum** 조합을 채택한다.

핵심 제약:

1. **ErrorCode 위치**: `template-domain` 모듈에 순수 Java enum으로 정의 (Spring 의존 없음)
2. **GlobalExceptionHandler 위치**: `template-adapter-input-api` 모듈에 `@RestControllerAdvice`로 배치
3. **레이어 경계**: domain/application 레이어는 순수 Java 예외(`RuntimeException` 계열)만 던짐; adapter에서 `ProblemDetail`로 변환
4. **확장 필드**: `ProblemDetail.setProperty("errorCode", errorCode.name())`으로 `errorCode` 확장 필드 추가
5. **Spring Boot 4 설정**: `spring.mvc.problemdetails.enabled=true` 활성화
6. **Spring MVC 내장 예외**: `ResponseEntityExceptionHandler` 상속으로 자동 변환

표준 RFC 9457 필드: `type`, `title`, `status`, `detail`, `instance`

```
domain/application → DomainException(ErrorCode) → adapter → ProblemDetail(+errorCode)
```

## 검토한 대안

| 대안 | 미채택 이유 |
|------|-----------|
| ProblemDetail 단일 표준 | 비즈니스 에러 코드 식별 불가, 클라이언트 처리 어려움 |
| ProblemDetail + ErrorCode + i18n | MessageSource 연동 복잡도 증가, 보일러플레이트 범위 초과 |
| 커스텀 ApiError DTO | RFC 표준 미준수, 클라이언트 라이브러리 호환성 저하 |

## 관련 문서

→ [architecture.md](../../.claude/rules/architecture.md) — 레이어 의존성 방향 규칙
→ [ADR-0001](0001-hexagonal-architecture-and-cqrs.md) — domain/application 순수 Java 원칙
→ [ADR-0003](0003-package-structure-and-naming.md) — adapter 패키지 구조
