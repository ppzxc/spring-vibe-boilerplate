---
status: accepted
date: 2026-04-04
decision-makers:
  - ppzxc
---

# Spring Cloud Contract로 API 계약 테스트 도입

## Context and Problem Statement

REST API 변경 시 Consumer(클라이언트)와 Producer(서버) 간 계약이 파손되는지 빌드 시점에 알 수 없다.
수동 통합 테스트 없이 API 계약을 자동 검증하고 Stub을 생성하여 Consumer 측 격리 테스트를 지원한다.

## Decision Drivers

* Groovy DSL이 표현력이 높고 조건부 응답 처리에 유리
* MockMvc 모드가 Servlet 기반 앱과 일관성 있음
* Stub JAR 생성으로 Consumer 측이 Producer 없이 테스트 가능

## Considered Options

* Spring Cloud Contract + Groovy DSL + MockMvc (채택)
* YAML DSL — 표현력 낮음, 조건부 응답 처리 어려움
* Pact — JVM 생태계 외부 도구, Spring 통합 추가 설정 필요

## Decision Outcome

Chosen option: "Spring Cloud Contract + Groovy DSL + MockMvc", because Spring 네이티브 도구이고 기존 MockMvc 테스트 패턴과 일관된다.

### Consequences

* Good, because Producer 계약 파손을 빌드 시 자동 감지
* Good, because Stub JAR로 Consumer 격리 테스트 지원
* Bad, because Groovy DSL 학습 필요
* Bad, because Spring Boot 4.x 호환성은 SCC 5.x(Boot 4 대응 버전) 사용으로 해결

### Confirmation

`./gradlew :template-adapter-input-api:test` — 계약에서 생성된 테스트가 모두 통과하면 성공.
`build/libs/*-stubs.jar` 파일이 생성되면 Stub 생성 성공.
