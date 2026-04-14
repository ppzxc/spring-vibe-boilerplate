# ADR-0005: jOOQ 선택, JPA 배제

## Status

Accepted

## Context

Persistence 기술로 JPA/Hibernate 또는 jOOQ를 선택해야 한다. 헥사고날 아키텍처 원칙(Domain 순수성)과 복잡한 쿼리 요구사항을 고려해야 한다.

JPA를 선택하면 `@Entity`를 Domain 클래스에 붙이거나, Domain과 JPA Entity를 분리하여 변환해야 한다. `@Entity` 어노테이션은 Domain에 기술 의존성을 주입하므로 ADR-0001 위반이다. JPA Entity 분리는 Domain ↔ JPA Entity 변환 레이어가 필요하고, JPA의 지연 로딩/프록시가 Domain 경계를 복잡하게 만든다.

## Decision

**jOOQ**를 Persistence 기술로 선택한다. JPA/Hibernate는 사용하지 않는다.

**이유**:
1. Domain 순수성: jOOQ는 Domain 클래스에 어노테이션 불필요. Domain은 순수 Java.
2. SQL 제어: 복잡한 JOIN, Window Function, UPSERT를 타입 안전하게 표현.
3. Optimistic Lock: `WHERE version = ?` 조건을 명시적으로 작성하여 동작이 명확.
4. jOOQ Codegen: DB 스키마 변경 시 컴파일 에러로 즉시 감지.

**위치**: `DSLContext`와 jOOQ Generated Classes는 **Outbound Adapter에서만** 사용한다.

**Mapper**: Domain ↔ jOOQ Record 변환은 수동 Mapper로 명시적으로 수행한다. MapStruct, ModelMapper 금지.

**H2 금지**: 테스트에서 H2 InMemory 사용 금지. Testcontainers + 실제 PostgreSQL.

## Consequences

### Positive
- Domain에 기술 어노테이션 없음 → ADR-0001 완전 준수
- SQL이 명시적 → 성능 문제 예측 가능
- Optimistic Lock 동작이 코드에 명시됨

### Negative
- JPA 비해 UPSERT, Batch Insert 등 직접 구현 필요
- jOOQ Codegen — DB 스키마 변경 시 코드 재생성 단계 필요
- N+1 문제가 없지만 JOIN 쿼리를 직접 작성해야 함
