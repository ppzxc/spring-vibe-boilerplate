---
status: accepted
date: 2026-04-05
decision-makers: ppzxc
---

# 스텁 모듈(adapter-input-ws, adapter-output-external) 유지 및 예제 강화

## Context and Problem Statement

보일러플레이트에는 두 개의 스텁 모듈이 있다.
`boilerplate-adapter-input-ws`는 WebSocket 입력 어댑터 골격이고,
`boilerplate-adapter-output-external`은 외부 HTTP API 호출 어댑터 골격이다.
두 모듈 모두 실제 비즈니스 로직 없이 의존성과 구조만 갖춘 상태로,
사용자가 이 패턴을 활용하기에는 예시가 부족하다는 지적이 있다.

## Decision Drivers

* 보일러플레이트의 목적: 실제 서비스 개발을 위한 패턴 참조점 제공
* adapter-input-ws: WebSocket이 선택적 기술이므로 최소 골격이라도 남겨 패턴을 시연해야 함
* adapter-output-external: Resilience4j CircuitBreaker/Retry 패턴을 이미 보여주는 의미있는 예제 코드가 있음
* 삭제 시 WebSocket 어댑터 배선 방법을 참조할 수 없어 사용자 DX 저하

## Considered Options

* 두 모듈 모두 삭제
* 두 모듈 모두 유지 (현 상태)
* 두 모듈 유지 + adapter-output-external에 예외 변환 패턴 시연 추가

## Decision Outcome

Chosen option: "두 모듈 유지 + adapter-output-external 예외 변환 패턴 추가", because
보일러플레이트 사용자가 WebSocket과 외부 HTTP 호출이 필요할 때 배선 참조점이 있어야 하며,
Resilience4j 패턴은 이미 충분한 예시이므로 기술 예외 → 도메인 예외 변환 패턴을 보강하면
출력 어댑터의 경계 책임을 더 명확히 전달할 수 있다.

### Consequences

* Good, because WebSocket/외부 HTTP 어댑터 패턴을 삭제 없이 유지하므로 사용자 참조 가능
* Good, because adapter-output-external에 예외 변환 패턴이 추가되어 헥사고날 경계 책임 명시
* Bad, because 스텁 모듈이 남아 있어 실제 서비스와 보일러플레이트의 경계가 다소 모호함

### Confirmation

* adapter-input-ws: 빌드 및 기존 아키텍처 테스트 통과 확인
* adapter-output-external: 기술 예외(RuntimeException) → 도메인 예외(DomainException) 변환 코드 존재 확인

## Pros and Cons of the Options

### 두 모듈 모두 삭제

* Good, because 코드베이스가 단순해짐
* Bad, because WebSocket/HTTP 어댑터 패턴 참조점 소실

### 두 모듈 모두 유지 (현 상태)

* Good, because 패턴 참조점 유지
* Bad, because adapter-output-external 스텁 코드가 불완전하여 오해 소지

### 두 모듈 유지 + 예외 변환 패턴 추가

* Good, because 헥사고날 아키텍처 경계 책임을 예제로 명시
* Good, because Resilience4j + 예외 변환 조합 패턴 제공
* Neutral, because 추가 구현 비용 발생

## More Information

관련 ADR: ADR-0021 (Resilience4j 전략), ADR-0007 (에러 처리 규칙)
실행 항목: Phase 6-3 (출력 어댑터 예외 변환 패턴 예제) 에서 구체 구현
