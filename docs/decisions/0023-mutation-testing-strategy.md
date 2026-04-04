---
status: accepted
date: 2026-04-04
decision-makers:
  - ppzxc
---

# Mutation Testing으로 테스트 품질 검증

## Context and Problem Statement

기존 단위 테스트가 실제로 버그를 탐지하는지 확인할 방법이 없다.
라인 커버리지 100%여도 assertion이 없거나 약한 경우 버그를 놓칠 수 있다.
Mutation Testing을 도입하여 테스트의 실효성을 정량적으로 검증한다.

## Decision Drivers

* domain/application 레이어는 순수 Java — Spring 없이 빠르게 실행 가능
* 기존 `info.solidsoft.gradle.pitest` Gradle 플러그인이 Gradle 생태계 표준
* mutation score 60% threshold를 최소 기준으로 설정 (스켈레톤 규모 고려)

## Considered Options

* info.solidsoft.gradle.pitest Gradle 플러그인
* Maven PIT 플러그인 (Gradle 프로젝트에 부적합)

## Decision Outcome

Chosen option: "info.solidsoft.gradle.pitest", because Gradle 네이티브 플러그인이고 JUnit 5 지원이 완전하다.

### Consequences

* Good, because 테스트 품질을 mutation score로 정량 측정 가능
* Good, because HTML 리포트로 살아남은 변이체(survived mutant)를 시각화
* Bad, because pitest 실행 시간이 일반 테스트보다 길다 (CI에서만 실행)

### Confirmation

`./gradlew :template-domain:pitest :template-application:pitest` — mutation score 60% 이상이면 통과.
