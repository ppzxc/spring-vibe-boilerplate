# ADR-0022 — Type-A Query UseCase Optional/List 반환

| 항목 | 내용 |
|------|------|
| 날짜 | 2026-05-11 |
| 상태 | Accepted |
| 결정자 | ppzxc |
| 관련 ADR | ADR-0004 (CQRS Level 1), ADR-0009 (Input Port 필수), ADR-0021 (Result 타입 도입) |

---

## Status

Accepted

---

## Context

CQRS Read Side의 조회 UseCase에서 "데이터가 없는 경우"를 어떻게 표현할지 결정해야 한다.

**Type-A 조회**(`Find*`, `Get*`, `List*`, `Search*`, `Show*` 접두어)는 **절대 조회(absolute lookup)**다. 존재 여부를 확인하는 것이 핵심이며, "없음"은 도메인 위반이 아닌 단순 부재다.

세 가지 선택지를 검토했다:

1. **`Optional<T>` / `List<T>` 직반환**: Java 표준 관용 패턴. Brian Goetz(2014)의 `Optional` 설계 의도 — "값이 없을 수 있음"을 표현.
2. **`Result<T, QueryError>` 통일**: absence를 `Failure`로 격상 → 의미 인플레이션. 단순 "없음"에 도메인 실패 의미 부여는 과도.
3. **`throw`로 직접 처리**: Application Service 내부에서 `NoSuchElementException`/Domain Exception throw. UseCase 시그니처가 실패 가능성을 숨김.

---

## Decision

`Find*`, `Get*`, `List*`, `Search*`, `Show*` 접두어로 시작하는 Query UseCase의 `execute` 메서드는 반드시 `Optional<T>` 또는 `List<T>`를 반환한다.

### 부재 처리 책임 분리

| 계층 | 역할 |
|------|------|
| **UseCase (Application)** | `Optional<T>` 또는 `List<T>` 그대로 반환. 도메인 예외로 승격 금지. |
| **Controller** | `.orElseThrow()` 호출 → `NoSuchElementException` |
| **`@RestControllerAdvice`** | `NoSuchElementException` → `404 Not Found` 매핑 |

"부재가 사실상 도메인 위반인 경우"(예: 로그인 중인 사용자 자신을 조회했는데 없음 — 데이터 정합성 문제)는 예외적으로 Application Service에서 Domain Exception을 throw할 수 있다. 이 경우는 Advice가 적절한 HTTP 상태로 매핑한다.

### 시그니처 예시

```java
// Type-A 단건 조회 — Optional 반환
public interface FindUserByIdUseCase {
    Optional<UserSummary> execute(FindUserByIdQuery query);
}

// Type-A 목록 조회 — List 반환
public interface ListRecentAuditLogsUseCase {
    List<AuditLogSummary> execute(ListRecentAuditLogsQuery query);
}
```

### Controller 처리 패턴

```java
// 단건 조회
@GetMapping("/{id}")
public ResponseEntity<UserDetailResponse> findById(@PathVariable String id) {
    return findUserByIdUseCase.execute(new FindUserByIdQuery(id))
        .map(UserDetailResponse::from)
        .map(ResponseEntity::ok)
        .orElseThrow();  // NoSuchElementException → Advice → 404
}

// 목록 조회
@GetMapping
public ResponseEntity<List<UserSummaryResponse>> list() {
    var summaries = listUsersUseCase.execute(new ListUsersQuery());
    return ResponseEntity.ok(summaries.stream().map(UserSummaryResponse::from).toList());
}
```

### ArchUnit 강제

`boilerplate-boot` 모듈 `ArchitectureTest`에 `typeA_query_usecase_execute_메서드는_Optional_또는_List_반환` 규칙이 등록되어 CI에서 자동 검증한다.

---

## Consequences

### 긍정

- Java 표준 `Optional`/`List` 패턴 — 팀 친화적이며 추가 학습 비용 없음.
- absence를 자연스럽게 표현. "없음"에 도메인 실패 의미를 부여하지 않음.
- Controller의 `.orElseThrow()` + Advice 한 줄로 404 매핑 완결.
- ArchUnit으로 Tier 1 자동 강제 — 위반 시 CI 빌드 실패.

### 부정

- Type-B Query UseCase 도입 시 Controller에 두 패턴(Optional 처리 + Result switch)이 공존.
  - 이는 의미 정확성(absence ≠ failure) 이득이 크므로 수용.

### 범위 명시

이 ADR은 `Find*`, `Get*`, `List*`, `Search*`, `Show*` 접두어에만 적용된다.
`Resolve*`, `Authorize*`, `Verify*`, `Compose*` 접두어(Type-B)는 별도 ADR에서 결정한다.
Type-B는 ADR-0021(Result 인프라)을 전제로 도입된다.
