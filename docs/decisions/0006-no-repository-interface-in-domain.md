# ADR-0006: Domain에 Repository 인터페이스 금지

## Status

Accepted

## Context

DDD에서 Repository 인터페이스를 Domain에 위치시키는 패턴이 있다. 그러나 Repository는 Persistence 기술과 결합된 개념이고, Domain이 Repository를 알면 Domain이 Persistence에 의존하게 된다.

## Decision

Domain 모듈에 Repository 인터페이스(`UserRepository`, `OrderRepository`)를 두지 않는다. Output Port(`LoadUserPort`, `SaveUserPort`)는 **Application 계층**에 위치한다 (D-10).

**근거**:
- Repository 인터페이스도 기술 의존성이다. Domain은 "데이터를 어디서 가져오는지" 모른다.
- Output Port는 Application이 Adapter에게 요구하는 계약. Application 계층에 위치가 적절하다.
- Domain이 Port를 알면 의존 방향이 뒤집힌다 (Domain → Application 금지).

## Consequences

### Positive
- Domain이 Persistence 개념 완전 무지 → ADR-0001 강화
- Application Service가 LoadPort, SavePort를 명시적으로 선언 → 의존성이 명확

### Negative
- Repository 패턴에 익숙한 개발자가 Port 개념을 새로 학습해야 함
- Spring Data JPA의 `CrudRepository` 패턴을 사용할 수 없음
