# ADR-0015: Domain에 로깅 프레임워크 금지

## Status

Accepted

## Context

ADR-0001의 세부 규칙. Domain 클래스에 SLF4J Logger, Log4j 등 로깅 프레임워크를 직접 사용하면 Domain이 로깅 인프라에 의존하게 된다.

## Decision

Domain 모듈에서 로깅 프레임워크(`org.slf4j.*`, `org.apache.logging.*`)를 사용하지 않는다 (D-3).

Domain 행위의 관찰은 4가지 방법으로 대체한다:
1. **Domain Event**: 비즈니스 사건을 이벤트로 발행 → Adapter가 이벤트 리스너로 로깅
2. **반환값**: UseCase Result DTO 기반으로 Configuration에서 메트릭/로그
3. **Domain Exception**: Adapter ControllerAdvice에서 로깅
4. **TX 프록시**: Configuration의 Observation 래핑

## Consequences

Domain 로직에서 로그를 직접 확인할 수 없어 디버깅 방식이 바뀜. Domain Event를 통한 간접 관찰로 더 명시적인 비즈니스 이벤트 설계를 유도함.
