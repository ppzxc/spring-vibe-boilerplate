# ADR-0021 — Result 타입 도입

| 항목 | 내용 |
|------|------|
| 날짜 | 2026-05-11 |
| 상태 | Accepted |
| 결정자 | ppzxc |
| 관련 ADR | ADR-0004 (CQRS Level 1), ADR-0019 (BC 기반 인가 배치), ADR-0022 (Type-A Query 반환 타입) |

---

## Status

Accepted

---

## Context

Java 표준 라이브러리는 functional error handling을 위한 `Result`/`Either` 타입을 제공하지 않는다.

현재 Application 계층에서 도메인 의미 있는 실패(인가 거부, 비즈니스 규칙 위반, 다단계 합성 실패)를 표현하는 두 가지 방법이 혼재한다:

1. **Checked Exception throw**: 호출 측이 시그니처만으로 실패 가능성을 읽기 어렵다. 컴파일러 강제가 없어 누락되기 쉽다.
2. **`Optional.empty()` + 위 레이어 null 처리**: `Optional`은 "값이 없음(absence)"을 표현하도록 설계되었으며(Brian Goetz 2014), "실패(failure with cause)"를 표현하기에 부적합하다.

**도메인 의미 있는 실패**가 누구인지, 왜 실패했는지를 1급 시민으로 표현하려면 표준 `Result` 인프라가 필요하다. 이는 향후 Command UseCase Result 마이그레이션, Type-B Query UseCase 도입의 **선결 조건**이다.

외부 라이브러리(Vavr, Arrow) 옵션도 검토했으나:
- 프로젝트에 이미 함수형 라이브러리 의존이 없다.
- Java 25 `sealed interface` + record deconstruction + pattern matching `switch`로 동등한 표현력이 가능하다.
- 외부 라이브러리 도입은 팀 학습 비용과 의존 확장을 수반한다.

---

## Decision

`boilerplate-shared-functional` 모듈을 신설하고 `Result<T, E>` sealed interface를 정의한다.

### 구조

```java
// boilerplate-shared-functional 모듈 — 순수 Java, 외부 의존 없음
public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    // 10개 핵심 메서드: success, failure, isSuccess, isFailure,
    //                  map, flatMap, mapError, fold, onSuccess, onFailure
}
```

### 핵심 원칙

1. **두 구현체**: `record Success<T, E>(T value)`, `record Failure<T, E>(E error)`.
2. **패턴 매칭 unwrap 강제**: `switch` 표현식 + record deconstruction 또는 `.fold()`만 사용. `isSuccess()` 분기 금지.
3. **BC별 sealed error 계층**: `E`는 반드시 해당 BC의 sealed interface(`{BC}Error`)여야 한다. 공통 super interface(`ApplicationError`) 불허 — BC 경계(ADR-0019) 준수.
4. **절대 조회(absence) 분리**: `Find*/Get*/List*/Search*/Show*` 접두어 UseCase는 `Optional`/`List`를 반환한다 (ADR-0022). Result는 도메인 의미 있는 실패에만 사용.
5. **인프라 예외 격리**: `IOException`, `JDBCException` 등 인프라 예외를 `E`로 wrap 금지.

### 도입 범위 (이 PR)

이 PR에서는 **인프라만 도입**한다. 도메인 코드의 Result 사용은 후속 PR에서 진행한다:
- Command UseCase → `Result<T, {BC}Error>` 마이그레이션 (후속 ADR)
- Type-B Query UseCase 도입 (후속 ADR)
- BC별 `{BC}Error` sealed interface 정의 (후속 PR)

---

## Consequences

### 긍정

- 도메인 의미 있는 실패를 1급 시민으로 표현 가능. 시그니처만으로 실패 가능성 명시.
- sealed `E` 타입으로 exhaustive switch 강제 → 런타임 누락 불가.
- BC별 sealed error로 ProblemDetail 매핑이 결정적이고 컴파일 타임에 완전.
- `flatMap` 체인으로 다단계 합성에서 early exit 자연스럽게 처리.
- 순수 Java — Spring/Vavr 의존 없음. Domain/Application 계층에서 안전하게 사용 가능.

### 부정

- 호출 측이 `.fold()`/`switch`로 unwrap해야 함 — `Optional`보다 약간의 boilerplate.
- BC별 sealed error 계층을 각 BC에서 정의해야 함 (추가 파일).

### 트레이드오프

도메인 의미 실패에만 한정 사용하고, 절대 조회 absence는 `Optional`로 분리한다 (ADR-0022). 이 분리가 Brian Goetz의 `Optional` 설계 의도(absence ≠ failure)를 보존하면서 Result 표현력도 확보한다.
