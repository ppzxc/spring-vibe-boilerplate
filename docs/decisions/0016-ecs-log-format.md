# ADR-0016: ECS JSON 구조화 로깅 채택

## Status

Accepted

## Context

서비스 관측성(observability) 확보를 위해 로그, 트레이스, 메트릭 세 신호를 traceId로 상관(correlate)해야 한다.
로그 형식이 비구조적(plain text)이면 파싱 비용이 높고, traceId 자동 주입이 불가능하다.

Spring Boot 4는 `logging.structured.format.console: ecs` 설정으로 ECS(Elastic Common Schema) JSON 형식을 네이티브 지원한다.

선택지:
- **ECS JSON** (채택): Spring Boot 4 네이티브 지원, OpenTelemetry traceId/spanId 자동 MDC 주입, Kibana/Grafana Loki 직접 파싱 가능.
- **Logstash JSON**: 별도 Logback 의존성 필요, ECS 표준 미준수.
- **Plain Text**: 파싱 오버헤드, 반구조화된 패턴 유지보수 비용.

## Decision

Spring Boot 4의 ECS JSON 구조화 로깅을 채택한다.

설정:
```yaml
logging:
  structured:
    format:
      console: ecs
```

OpenTelemetry MDC 자동 주입으로 모든 로그에 `traceId`, `spanId` 포함. 별도 코드 불필요.

## Consequences

- ✅ traceId/spanId 자동 주입 → 로그-트레이스-메트릭 상관 분석 가능
- ✅ ECS 표준 준수 → Kibana, Grafana Loki 직접 연동
- ✅ Spring Boot 4 네이티브 지원 → 추가 의존성 없음
- ⚠️ 개발 환경 가독성 감소 → `application-dev.yml`에서 plain text 오버라이드 허용
