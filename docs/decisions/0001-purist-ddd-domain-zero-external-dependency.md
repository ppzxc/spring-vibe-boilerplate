# ADR-0001: 순수주의 DDD — Domain 모듈 외부 의존 제로

## Status

Accepted

## Context

사내 IDP 서버 + Resource Server를 새로 설계하면서 도메인 로직의 장기적 유지보수성과 테스트 용이성을 확보해야 한다. 과거 레거시 코드에서 Domain 객체에 Spring `@Entity`, Jackson `@JsonProperty`, jOOQ 레코드가 혼재되어 프레임워크 업그레이드 시 Domain을 함께 수정해야 했고, Domain 테스트가 Spring Context에 의존하여 느리고 불안정했다.

## Decision

Domain 모듈(`boilerplate-{bc}-domain`)은 **순수 Java**로만 구성한다. 외부 의존성은 완전 제로.

금지 import:
- `org.springframework.*` — 프레임워크 어노테이션 금지
- `org.jooq.*` — DB 기술 금지
- `com.fasterxml.*` — 직렬화 기술 금지
- `org.slf4j.*`, `org.apache.logging.*` — 로깅 프레임워크 금지
- `jakarta.*` — Jakarta EE 스펙 금지
- `org.jspecify.*` — Null-safety 어노테이션 금지 (Domain은 `Objects.requireNonNull`로)
- `java.time.Clock` — Clock 주입 금지 (`Instant`를 파라미터로 받음)

`Instant.now()` 직접 호출도 금지. Application이 `Instant`를 파라미터로 전달한다.

Gradle 멀티모듈이 1차 방어선: `domain` 모듈의 `build.gradle.kts`에 의존성 선언이 없으면 컴파일 자체가 불가능하다. ArchUnit이 2차 방어선으로 패키지 의존 방향을 런타임에 검증한다.

## Consequences

### Positive
- Domain 테스트가 Spring Context 없이 순수 Java로 실행 → 밀리초 단위 빠른 피드백
- 프레임워크 업그레이드 시 Domain 코드 변경 불필요
- Domain 로직이 기술 세부사항에 오염되지 않음
- Aggregate, VO, Domain Event가 이식 가능(portable)

### Negative
- Null-safety를 JSpecify 어노테이션이 아닌 `Objects.requireNonNull`로 처리 → 런타임 검증
- Domain에서 로깅 불가 → Domain Event, 반환값, 예외를 통해 간접 관찰
- 기술적 편의 기능(JPA `@Version`, Jackson 어노테이션)을 Adapter 계층에서 별도 처리 필요
