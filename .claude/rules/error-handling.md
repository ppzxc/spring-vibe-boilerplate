---
description: Result<T,E> sealed 패턴, BC별 Error 계층, 절대 규칙 7개, 안티패턴 12개
alwaysApply: true
---

# Error Handling Rules

Result 타입 사용 규칙 — 항상 로드.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.
>
> 근거: ADR-0021

---

## 1. Result 사용 절대 규칙

- MUST: 도메인 의미 있는 실패는 `Result<T, E>`로 표현한다. E는 BC별 sealed interface여야 한다.
- MUST: BC별로 sealed Error 계층을 정의한다. 공통 super interface(`ApplicationError`)는 두지 않는다 (ADR-0019 BC 경계 준수).
- MUST: Success/Failure unwrap은 `switch` + record deconstruction 또는 `.fold()`로 수행한다.
- MUST NOT: `result.isSuccess()` / `result.isFailure()` 분기를 사용한다 — `switch`/`fold`만 사용.
- MUST NOT: 절대 조회 부재(`Find*/Get*/List*/Search*/Show*`)에 Result를 사용한다 — Optional/List를 사용한다 (ADR-0022).
- MUST NOT: 인프라 예외(`IOException`, `JDBCException`, `DataAccessException` 등)를 `E`로 wrap한다 — 진짜 도메인 실패만 표현.
- MUST NOT: `Result<Result<T, E>, E>` 중첩을 만든다 — `flatMap`으로 평탄화.

---

## 2. BC별 sealed Error 계층

각 BC의 application 모듈에 `{BC}Error` sealed interface를 정의한다. 공통 super는 두지 않는다.

```java
// boilerplate-identity-application — 예시
public sealed interface IdentityError
    permits IdentityError.NotFound,
            IdentityError.Forbidden,
            IdentityError.AlreadyExists,
            IdentityError.BusinessRuleViolated {

  record NotFound(String resourceType, String resourceId) implements IdentityError {}

  record Forbidden(String reason) implements IdentityError {}

  record AlreadyExists(String resourceType, String key) implements IdentityError {}

  record BusinessRuleViolated(String code, String message) implements IdentityError {}
}
```

---

## 3. Result 사용 표준 패턴

### 3.1 생성

```java
return Result.success(authorizedPermission);
return Result.failure(new IdentityError.Forbidden("user:deactivate"));
```

### 3.2 변환 (map)

```java
Result<UserResponse, IdentityError> mapped = authorizeResult.map(UserResponse::from);
```

### 3.3 합성 (flatMap)

```java
return loadUserResult
    .flatMap(user -> checkPermissionResult(user))
    .flatMap(user -> issueTokenResult(user));
```

### 3.4 unwrap — switch + record deconstruction

```java
return switch (authorizePermissionUseCase.execute(query)) {
  case Result.Success<AuthorizedPermission, IdentityError>(var permission) ->
      ResponseEntity.ok(PermissionResponse.from(permission));
  case Result.Failure<AuthorizedPermission, IdentityError>(IdentityError.Forbidden f) ->
      ResponseEntity.status(403).body(ProblemDetail.forStatus(403));
  case Result.Failure<AuthorizedPermission, IdentityError>(IdentityError.NotFound n) ->
      ResponseEntity.status(404).body(ProblemDetail.forStatus(404));
  case Result.Failure<AuthorizedPermission, IdentityError>(IdentityError.AlreadyExists a) ->
      ResponseEntity.status(409).body(ProblemDetail.forStatus(409));
  case Result.Failure<AuthorizedPermission, IdentityError>(IdentityError.BusinessRuleViolated b) ->
      ResponseEntity.status(422).body(ProblemDetail.forStatus(422));
};
```

### 3.5 unwrap — fold

```java
HttpStatus status = result.fold(
    success -> HttpStatus.OK,
    error -> switch (error) {
      case IdentityError.NotFound n -> HttpStatus.NOT_FOUND;
      case IdentityError.Forbidden f -> HttpStatus.FORBIDDEN;
      case IdentityError.AlreadyExists a -> HttpStatus.CONFLICT;
      case IdentityError.BusinessRuleViolated b -> HttpStatus.UNPROCESSABLE_ENTITY;
    });
```

---

## 4. 예외 허용 — throw가 정당한 3가지 경우

다음 3가지는 Result로 wrap하지 않고 예외를 throw한다:

1. **시스템 불변식 위반** (`IllegalArgumentException`, `NullPointerException`, `IllegalStateException`)
   - VO Compact Constructor의 형식 검증 실패
   - `Objects.requireNonNull` 실패
   - "발생해서는 안 되는" 프로그래밍 오류
2. **인프라 예외** (`OptimisticLockException`, `DataAccessException`, `IOException`)
   - DB 충돌, 네트워크 실패 — 도메인 의미 없음
3. **절대 조회 부재** (Type-A: `Find*/Get*/List*/Search*/Show*`)
   - `Optional` 반환. 부재는 도메인 위반이 아님 (ADR-0022)

---

## 5. 안티패턴 (MUST NOT)

1. `Result<T, Exception>` — Exception을 E로 wrap (인프라 예외와 도메인 실패 혼동)
2. `Result<T, String>` — String 에러 (sealed 강제 위반, 매핑 불가)
3. `result.isFailure() ? ... : result.value()` 식의 `isXxx` 분기 (switch/fold 사용)
4. Type-A Query UseCase가 Result 반환 (`Find*/Get*Query` → Result 금지, ADR-0022)
5. `Result<Result<T, E>, E>` 중첩 (flatMap으로 평탄화)
6. 동일 함수가 Success와 throw를 섞어서 사용 (Result 반환 함수는 throw 금지)
7. Failure를 if/else로 분기 후 default `throw new RuntimeException` (모든 sealed 변형 처리)
8. `.orElseThrow()` 없이 `Optional`을 throw로 unwrap하는 Application Service (Controller가 처리)
9. 공통 super interface `ApplicationError` 도입 (BC 경계 흐림 — BC별 sealed로 유지)
10. `Result.success(null)` (null value는 `Optional.empty()`로 표현)
11. DTO에 `Result` 필드 포함 (직렬화 시그니처 오염 — Controller에서 unwrap 후 DTO)
12. Domain 모듈에서 `Result` import (Application의 책임 — Domain은 throw로 충분)

---

## 6. AI 코드 생성 자기검증 체크리스트

Result를 사용한 코드 생성 후 반드시 확인:

- [ ] Error 타입이 sealed interface인가?
- [ ] Failure의 변형이 모두 record인가?
- [ ] unwrap이 switch/fold인가? (`isSuccess()` 분기 없음)
- [ ] switch가 모든 sealed 변형을 처리하는가? (`default` 없음)
- [ ] 같은 함수가 Result 반환과 throw를 섞지 않는가?
- [ ] `Result<T, Exception>` 또는 `Result<T, String>` 사용이 없는가?
- [ ] Type-A Query UseCase가 Result를 반환하지 않는가?
- [ ] `Result<Result<...>>` 중첩이 없는가?
- [ ] DTO에 Result 필드가 없는가?
- [ ] Domain 모듈에 Result import가 없는가?

---

## fallback 지시문

> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> `docs/decisions/0021-result-type-introduction.md`와 `docs/decisions/0022-type-a-query-usecase-optional-list-return.md`를 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
