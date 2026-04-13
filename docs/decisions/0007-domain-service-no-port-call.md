# ADR-0007: Domain Service에서 Port 호출 금지

## Status

Accepted

## Context

복잡한 비즈니스 로직이 여러 Aggregate에 걸쳐 있을 때 Domain Service를 사용한다. 일부 패턴에서 Domain Service가 Repository(또는 Port)를 직접 호출하는 경우가 있다.

## Decision

Domain Service는 Port를 직접 호출하지 않는다 (D-11). 필요한 데이터는 Application Service가 Port를 통해 로드한 후 Domain Service에 파라미터로 전달한다.

**이유**:
1. Domain Service가 Port를 알면 Domain이 Application 계층의 Port에 의존 → 의존 방향 위반
2. Domain Service 테스트 시 Port Mock이 필요해져 순수 단위 테스트 불가
3. "데이터를 어떻게 가져오는지"는 Application의 오케스트레이션 책임

**패턴**:
```
Application Service:
    1. LoadPort로 필요한 데이터 조회
    2. Domain Service 또는 Aggregate 행위에 파라미터로 전달
    3. SavePort로 결과 저장
```

## Consequences

### Positive
- Domain Service가 순수 Java 메서드 → 파라미터만으로 단위 테스트 가능
- 의존 방향이 항상 바깥 → 안쪽으로 일관

### Negative
- Application Service에서 여러 LoadPort 호출 후 Domain Service에 전달 → Application이 길어질 수 있음
