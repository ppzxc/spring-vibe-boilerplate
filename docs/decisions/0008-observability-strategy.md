---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 관측성 전략: Spring Boot 4 네이티브 구조화 로깅 + SLF4J fluent API + Actuator + OTel OTLP

## 배경 및 문제

분산 서비스 환경에서 로그, 메트릭, 트레이싱 세 축의 관측성이 없으면 장애 진단이 어렵다.
Spring Boot 4는 구조화 로깅을 네이티브로 지원하여 외부 인코더 라이브러리가 불필요해졌다.
OpenTelemetry가 CNCF graduated 프로젝트로 관측성 업계 표준이 되었고,
벤더 중립적 계측을 통해 백엔드 수집기를 자유롭게 선택할 수 있다.

## 결정 기준

* Spring Boot 4 네이티브 기능 최대 활용 (외부 의존성 최소화)
* 로컬/CI 환경에서 구조화 로그 출력
* 분산 트레이싱 지원 (requestId, traceId 전파)
* 수집 백엔드 벤더 중립성 (Jaeger, Tempo, Datadog 등 선택 가능)

## 결정

**Spring Boot 4 네이티브 구조화 로깅 + SLF4J fluent API + Actuator + OTel OTLP** 조합을 채택한다.

### 1. 구조화 로깅

핵심 제약:

1. **인코더**: Spring Boot 4 네이티브 `StructuredLogEncoder` 사용 — `logstash-logback-encoder` 의존성 불필요
2. **설정**: `logging.structured.format.console: logstash` 및 `logging.structured.format.file: logstash`
3. **API**: SLF4J fluent API 강제 — `logger.atInfo().addKeyValue("key", val).log("message")`
4. **MDC 필수 필드**: `requestId`, `traceId` — 모든 로그에 포함

### 2. Actuator

5. **활성화 엔드포인트**: `health`, `info`, `metrics`
6. **보안**: prod 환경에서 `/actuator/**` 엔드포인트 인증 필수

### 3. OpenTelemetry (OTLP)

7. **의존성**: `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp`
8. **수집 백엔드**: 프로젝트별 선택 (Jaeger, Grafana Tempo, Datadog 등) — 보일러플레이트에서 고정하지 않음
9. **OTLP 엔드포인트**: `management.otlp.tracing.endpoint` 환경변수로 주입

## 검토한 대안

| 대안 | 미채택 이유 |
|------|-----------|
| 구조화 로깅만 | 분산 트레이싱 부재, 프로덕션 장애 진단 어려움 |
| 구조화 로깅 + Actuator (OTel 없음) | 서비스 간 트레이스 전파 불가 |
| logstash-logback-encoder | Spring Boot 4 네이티브 대체 가능, 외부 의존성 불필요 |
| Zipkin exporter | OTLP가 더 범용적, Zipkin은 벤더 종속 |

## 관련 문서

→ [ci-tools.md](../../.claude/rules/ci-tools.md) — CI 파이프라인 구성
→ [ADR-0006](0006-ci-pipeline-strategy.md) — GitHub Actions 잡 구성
→ [ADR-0011](0011-configuration-strategy.md) — 환경변수 주입 패턴
