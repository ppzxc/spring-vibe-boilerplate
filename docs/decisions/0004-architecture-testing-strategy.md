---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 아키텍처 테스트 전략: ArchUnit

## Context and Problem Statement

Hexagonal Architecture의 레이어 의존성 규칙은 개발자와 AI 에이전트 모두 실수로 위반할 수 있다.
코드 리뷰만으로는 이를 완전히 방지하기 어렵고, 위반이 누적되면 아키텍처가 서서히 무너진다.
의존성 규칙을 자동으로 검증하는 수단이 필요하다.

## Decision Drivers

* 의존성 방향 규칙을 테스트 코드로 명문화
* 빌드 실패로 규칙 위반 즉시 감지
* 초기 빈 상태에서도 테스트가 통과해야 함 (보일러플레이트 특성)
* 규칙 추가가 쉬워야 함

## Decision Outcome

Chosen option: "ArchUnit (1.3.0)", because JUnit 5 통합, 선언적 DSL, 빌드 파이프라인 자연 통합, Java/Spring 생태계 사실상 표준.

**선택 이유:**
* JUnit 5와 통합되어 기존 테스트 인프라 재사용
* 선언적 DSL로 아키텍처 규칙을 명확하게 표현
* 빌드 파이프라인에 자연스럽게 통합
* Java/Spring 생태계에서 사실상 표준

**`allowEmptyShould(true)` 필수:**
보일러플레이트 초기 상태에서 클래스가 없어도 테스트가 통과해야 한다.
클래스가 추가될 때부터 규칙이 적용된다.

## 강제하는 규칙 목록

### Domain 레이어 (`template-domain`)

| 규칙 | 이유 |
|------|------|
| Spring 의존 금지 | Domain은 프레임워크에 독립적이어야 함 |
| JPA 의존 금지 | 영속성 기술은 Outbound Adapter 책임 |
| 허용 패키지: `domain`, `java`, `javax`, `jakarta.validation`, `lombok`, `jspecify` | 최소 의존 유지 |

### Application 레이어 (`template-application`)

| 규칙 | 이유 |
|------|------|
| Spring 의존 금지 | UseCase 로직은 프레임워크에 독립적 |
| 허용 패키지: `application`, `domain`, `java`, `javax`, `jakarta.validation`, `lombok`, `jspecify` | 최소 의존 유지 |
| Outbound Port는 반드시 interface | 구현체는 Adapter에 위치 |
| Inbound Command Port (`..port.input.command..*UseCase`)는 interface | 계약 명확화 |
| Inbound Query Port (`..port.input.query..*Query`)는 interface | 계약 명확화 |
| Query Service → Command Outbound Port 의존 금지 | CQRS 경계 유지 |

## 테스트 위치

| 테스트 클래스 | 모듈 | 패키지 |
|-------------|------|--------|
| `DomainArchitectureTest` | `template-domain` | `io.github.ppzxc.template.domain.architecture` |
| `ApplicationArchitectureTest` | `template-application` | `io.github.ppzxc.template.application.architecture` |

## 향후 규칙 추가 지침

Adapter 간 의존성 격리, 순환 의존 금지 등 추가 규칙이 필요하면:
1. 해당 모듈에 `*ArchitectureTest.java` 추가
2. `allowEmptyShould(true)` 반드시 포함
3. [architecture.md](../.claude/rules/architecture.md)에 규칙 문서화

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| Dependency-Check (OWASP) | 보안 취약점 스캔 목적, 아키텍처 규칙 강제 불가 |
| jMolecules Enforcer | Spring Modulith에 최적화, Gradle 통합 복잡 |
| 코드 리뷰만으로 강제 | 인간/AI 실수 방지 불충분, 자동화 필요 |
| Checkstyle 패키지 규칙 | 소스 레벨 검사만 가능, 바이트코드 의존성 분석 불가 |

## More Information

→ [architecture.md](../.claude/rules/architecture.md) — 의존성 규칙 상세
→ [testing.md](../.claude/rules/testing.md) — 테스트 전략 전반
