# Audit BC Ubiquitous Language

> **Living Document.** Audit BC의 핵심 용어 사전.
> 동일한 용어가 코드(클래스명, 메서드명, 필드명)에 그대로 반영된다.

---

## 용어 사전

| 용어(한국어) | 용어(영어 코드명) | 정의 | 유사 용어와의 차이 |
|-------------|----------------|------|-----------------|
| 감사 로그 | `AuditLog` | 시스템 내에서 발생한 비즈니스 이벤트를 추적하기 위해 append-only로 기록한 단건 로그. 한 번 기록되면 변경·삭제 불가. | `Notification`과 달리 상태 전이가 없음(PENDING→SENT 등). 단순 기록 전용. |
| 감사 로그 ID | `AuditLogId` | 각 감사 로그 행을 유일하게 식별하는 UUIDv7 기반 식별자. | Identity BC의 `UserId`와 별개. Audit BC 내부에서만 사용. |
| 감사 대상 사용자 ID | `AuditedUserId` | 감사 로그가 기록된 대상(subject) 사용자의 ID. Identity BC의 `UserId`와 값은 동일하지만 Audit BC 내부 ACL VO로 격리된 표현. | Identity BC의 `UserId`를 직접 참조하지 않음. 경계를 넘어올 때 `AuditedUserId`로 변환(Conformist + ACL). |
| 감사 이벤트 종류 | `AuditEventType` | 감사 로그가 어떤 비즈니스 이벤트에 의해 생성되었는지 나타내는 열거형. 현재 값: `USER_REGISTERED`. | `event_type` 컬럼에 VARCHAR(50)으로 저장. 신규 이벤트 추가 시 enum 항목 추가 + listener 1개 추가. |
| 감사 페이로드 | `AuditPayload` | 감사 이벤트의 세부 내용을 담은 JSON 문자열 VO. 1~10,000자 범위. 구조는 이벤트 종류마다 다를 수 있음. | PostgreSQL JSONB 컬럼에 저장. 스키마 변경 없이 페이로드 필드만 확장 가능. |
| 발생 시각 | `occurredAt` | 원래 비즈니스 이벤트가 발생한 UTC 시각 (`Instant`). Identity BC에서 발행된 Integration Event의 `occurredAt` 그대로 보존. | `recordedAt`과 구분. 이벤트가 발생한 시점 ≠ 로그가 적재된 시점. |
| 기록 시각 | `recordedAt` | Audit BC의 `RecordUserRegisteredAuditService`가 감사 로그를 DB에 기록한 UTC 시각 (`Instant`). | `occurredAt`과 구분. 시스템 장애·재전송 등으로 두 값이 다를 수 있음. |
| 감사 적재 UseCase | `RecordUserRegisteredAuditUseCase` | `UserRegisteredIntegrationEvent` 수신 후 `audit_log` 테이블에 1행을 INSERT하는 UseCase. void 반환. | 조회 UseCase(`FindAuditLogsBySubjectUseCase` 등)와 달리 Command Side. |
| 주제별 감사 조회 UseCase | `FindAuditLogsBySubjectUseCase` | 특정 사용자 ID(`subjectUserId`)에 대한 감사 로그 목록을 조회하는 UseCase. | `ListRecentAuditLogsUseCase`와 달리 대상 사용자 기준 조회. |
| 최근 감사 조회 UseCase | `ListRecentAuditLogsUseCase` | 전체 감사 로그를 최근순으로 조회하는 UseCase. `limit` 상한: 1~1,000. | `FindAuditLogsBySubjectUseCase`와 달리 전체 대상 최근순 조회. |

---

## 경계 노트

- **Conformist 패턴**: Audit BC는 Identity BC의 `UserRegisteredIntegrationEvent`를 수정 없이 수용한다. shared-event 모듈을 무수정 참조.
- **ACL VO**: Identity BC의 `UserId`가 Audit BC 경계를 넘어오면 `AuditedUserId`로 변환된다. 두 VO는 값이 동일하지만 의미와 소속 컨텍스트가 다르다.
- **append-only 불변식**: `AuditLog`는 `create()` 이후 상태 변경 메서드가 없다. UPDATE 경로는 현재 설계에 없으며, Optimistic Lock도 적용하지 않는다(INSERT-only).
- **HTTP API 없음(YAGNI)**: 현재 구현에서는 Query Port + UseCase만 정의하고 REST 엔드포인트는 제공하지 않는다. 관리자 콘솔 요구 시 `adapter-input-api` 모듈을 추가한다.
