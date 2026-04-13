# ADR-0010: UseCase 간 직접 호출 금지

## Status

Accepted

## Context

UseCase A에서 UseCase B를 직접 호출하면 트랜잭션이 중첩되고, 순환 의존이 발생할 수 있다. TX 프록시로 각 UseCase를 감싸는 구조에서 중첩 호출은 TX 경계를 모호하게 만든다.

## Decision

UseCase에서 다른 UseCase를 직접 호출하지 않는다 (A-8). 여러 UseCase가 협력해야 하면 이벤트 기반으로 분리한다.

**패턴**:
```
UseCase A → SavePort → Outbox → EventHandler → UseCase B (별도 TX)
```

## Consequences

UseCase 간 직접 의존이 없어지고 각 UseCase가 독립 TX로 실행된다. 이벤트 기반 분리로 인해 최종 일관성(Eventual Consistency)을 수용해야 한다.
