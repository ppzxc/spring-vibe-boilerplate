# ADR-0003: Gradle 멀티모듈 + Spring Modulith 하이브리드

## Status

Accepted

## Context

모놀리스로 시작하되 BC별 독립 배포 가능성을 열어두어야 한다. 순수 Gradle 멀티모듈만 사용하면 Outbox 패턴을 직접 구현해야 하고, Spring Modulith만 사용하면 컴파일 타임 의존성 강제가 불가능하다. 두 가지를 모두 원한다.

## Decision

**Gradle 멀티모듈(컴파일 타임)** + **Spring Modulith(런타임 Outbox)**를 병행한다.

| 방어선 | 도구 | 역할 |
|--------|------|------|
| 1차 (컴파일) | Gradle 멀티모듈 | 허용되지 않은 의존성 → 컴파일 실패 |
| 2차 (테스트) | ArchUnit | 패키지 의존 방향 런타임 검증 |
| Outbox | Spring Modulith EventPublicationRegistry | `event_publication` 트랜잭셔널 Outbox 자동화 |
| 런타임 | `@Modulithic` + `ApplicationModules.verify()` | 허용되지 않은 모듈 간 참조 감지 |

**모듈 네이밍 패턴**: `boilerplate-{bc}-{layer}`

**Common 모듈 금지**: 공유는 `shared-event`(이벤트 계약만) 또는 `test-support`(픽스처만)로 제한.

**BC 확장**: 신규 BC 추가 시 `settings.gradle.kts`에 모듈 등록 + `@Modulithic`의 `sharedModules` 확인.

## Consequences

### Positive
- 컴파일 타임에 의존성 방향 강제 → 구조 오염이 빌드 단계에서 발견됨
- Spring Modulith Outbox로 트랜잭셔널 이벤트 발행 자동화
- Modulith 단계에서 단일 배포 → 필요 시 마이크로서비스로 추출 용이

### Negative
- Gradle 모듈 + Modulith 패키지 인식 이중 관리 오버헤드
- 모듈 수가 BC × 계층만큼 늘어남 (BC당 최대 7개 모듈)
- `settings.gradle.kts` 신규 BC 추가 시 수동 등록 필요
