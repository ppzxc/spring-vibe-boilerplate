# Error Handling Rules

## 레이어별 예외 처리 규칙 [ADR-0007]

| 레이어 | 허용 예외 타입 | 금지 |
|--------|--------------|------|
| domain | 순수 Java 예외 (`RuntimeException`, `IllegalArgumentException`, `IllegalStateException`) | Spring, HTTP 예외 |
| application | 순수 Java 예외 (domain 예외 전파 또는 재정의) | Spring, HTTP 예외 |
| adapter (입력) | Spring `ResponseEntity`, `ProblemDetail` 변환 | 예외 노출 그대로 |

## ErrorCode 규칙

- 위치: `io.github.ppzxc.template.domain` (template-domain 모듈)
- 타입: 순수 Java `enum` (Spring 의존 금지)
- 예시 값:

```java
public enum ErrorCode {
    NOT_FOUND,
    INVALID_ARGUMENT,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL_ERROR
}
```

## GlobalExceptionHandler 규칙

- 위치: `template-adapter-input-api` 모듈
- 클래스명: `GlobalExceptionHandler`
- 어노테이션: `@RestControllerAdvice`
- 상속: `ResponseEntityExceptionHandler`
- Spring Boot 4 필수 설정 (`application.yml`):

```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

## 에러 응답 형식 (RFC 9457 + 확장)

```json
{
  "type": "https://example.com/errors/invalid-argument",
  "title": "Invalid Argument",
  "status": 400,
  "detail": "필드 'name'은 비워둘 수 없습니다.",
  "instance": "/api/v1/orders",
  "errorCode": "INVALID_ARGUMENT"
}
```

- `errorCode` 필드: `ErrorCode` enum 값을 문자열로 포함 (클라이언트 분기 처리용)
- `ProblemDetail.setProperty("errorCode", errorCode.name())` 으로 확장

## 금지 패턴

- adapter 레이어 밖으로 `HttpStatus` 또는 `ResponseEntity` 누출 금지
- domain/application 에서 `@ResponseStatus` 어노테이션 사용 금지
- 예외 메시지에 스택 트레이스 포함 금지 (프로덕션)
