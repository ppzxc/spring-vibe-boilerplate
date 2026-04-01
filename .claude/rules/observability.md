# Observability Rules

## 구조화 로깅 [ADR-0008]

Spring Boot 4 네이티브 구조화 로깅 사용. 외부 인코더(logstash-logback-encoder) 사용 금지.

```yaml
# application.yml
logging:
  structured:
    format:
      console: logstash
      file: logstash
```

## SLF4J Fluent API 강제

| 구분 | 패턴 |
|------|------|
| 허용 | `logger.atInfo().addKeyValue("orderId", id).log("주문 생성")` |
| 금지 | `logger.info("주문 생성: {}", id)` — 구조화 필드 누락 |

- 키-값 쌍은 `addKeyValue(key, value)` 로만 추가
- 민감 정보(패스워드, 토큰)는 로그 키-값에서 제외

## MDC 필수 필드

| 필드 | 설명 | 주입 위치 |
|------|------|----------|
| `requestId` | HTTP 요청 고유 ID (UUID) | `OncePerRequestFilter` |
| `traceId` | OpenTelemetry 분산 추적 ID | Micrometer 자동 주입 |

## Actuator 엔드포인트 규칙

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: when-authorized
```

- 노출 필수: `health`, `info`, `metrics`
- `health` 상세 정보: 인증된 요청에만 공개
- `env`, `beans` 등 민감 엔드포인트 노출 금지

## OpenTelemetry 의존성 규칙

- 의존성 위치: `template-boot-api` 모듈
- 필수 의존성:
  - `micrometer-tracing-bridge-otel`
  - `opentelemetry-exporter-otlp`
- 수집 백엔드 설정: 환경변수 `OTLP_ENDPOINT`로 주입 (프로젝트별 선택)

```yaml
management:
  otlp:
    tracing:
      endpoint: ${OTLP_ENDPOINT:}
```
