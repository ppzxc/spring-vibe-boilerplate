# Error Handling Rules

## 레이어별 예외 처리 규칙 [ADR-0007]

| 레이어 | 허용 예외 타입 | 금지 |
|--------|--------------|------|
| domain | 순수 Java 예외 (`RuntimeException`, `IllegalArgumentException`, `IllegalStateException`) | Spring, HTTP 예외 |
| application | 순수 Java 예외 (domain 예외 전파 또는 재정의) | Spring, HTTP 예외 |
| adapter (입력) | Spring `ResponseEntity`, `ProblemDetail` 변환 | 예외 노출 그대로 |

## 구현 원칙 [ADR-0007]

- domain 레이어에 ErrorCode enum을 정의할 것 (Spring 의존 금지)
- adapter-input-api에서 ProblemDetail(RFC 9457)로 예외를 변환할 것
- 에러 응답에 errorCode 필드를 포함할 것 (클라이언트 분기 처리용)

## 에러 코드 체계 [ADR-0017]

- ErrorCode enum은 AIP-193 canonical error codes를 따를 것 [ADR-0017]
- ProblemDetail 확장 필드에 `errorCode` (AIP 코드명)과 `details` (FieldViolation 배열)을 포함할 것 [ADR-0017]
- 새 에러 코드 추가 시 AIP-193 표준 코드를 우선 사용할 것 — 커스텀 코드는 표준으로 표현 불가능한 경우에만 추가 [ADR-0017]

## 금지 패턴

- adapter 레이어 밖으로 `HttpStatus` 또는 `ResponseEntity` 누출 금지
- domain/application 에서 `@ResponseStatus` 어노테이션 사용 금지
- 예외 메시지에 스택 트레이스 포함 금지 (프로덕션)
