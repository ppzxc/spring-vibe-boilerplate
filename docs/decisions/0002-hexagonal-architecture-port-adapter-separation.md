# ADR-0002: 헥사고날 아키텍처 — Port/Adapter 4계층 분리

## Status

Accepted

## Context

전통적인 계층형 아키텍처(Controller → Service → Repository)에서는 Service가 Persistence 기술(JPA, jOOQ)을 직접 참조하여 기술 교체 비용이 크고, Inbound Adapter(Controller)가 Domain 객체를 직접 참조하여 계층 간 결합도가 높았다. 새 프로젝트에서는 각 계층이 기술 교체 없이 독립적으로 테스트 가능해야 한다.

## Decision

4계층 헥사고날 아키텍처를 적용한다.

```
[Domain] ← [Application] ← [Adapter] ← [Configuration]
```

의존 방향은 항상 바깥 → 안쪽. Domain은 아무것도 모른다.

**Port 분리 원칙**:
- Input Port (UseCase 인터페이스): `application/port/in/`
- Output Port (Load/Save/Query): `application/port/out/`

**규칙**:
- AD-1: Inbound Adapter는 Input Port 인터페이스에만 의존. Domain 직접 참조 금지.
- AD-2: Adapter 간 직접 참조 금지. Application Port 경유.
- A-1: Application 모듈은 Domain만 의존. Spring, jOOQ 등 기술 의존 금지.
- T-2: Controller에서 트랜잭션 시작 금지.
- T-3: Port 구현체 내부에서 독립 TX 시작 금지.

## Consequences

### Positive
- 기술 교체 시 Adapter만 교체. Domain, Application 불변.
- Application Service를 `new`로 생성하여 Spring 없이 단위 테스트 가능.
- Controller가 UseCase 인터페이스만 알고 Domain 객체 직접 참조 불가.

### Negative
- 모든 계층 경계에서 명시적 DTO 변환(Mapper) 필요 → 보일러플레이트 증가
- 단순 CRUD에도 Input Port 인터페이스 + Mapper + UseCase 구현체 필요
- 파일 수가 전통 계층형보다 많아짐
