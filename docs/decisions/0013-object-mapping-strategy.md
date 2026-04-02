---
status: accepted
date: 2026-04-02
decision-makers: ppzxc
---

# 객체 변환 전략: 하이브리드 (MapStruct + static factory)

## Context and Problem Statement

Hexagonal Architecture에서 adapter 레이어는 외부 표현(REST DTO, JPA Entity, Cache Entry)과 도메인 모델 간 변환을 담당한다. 변환 코드의 일관된 전략이 없으면 필드 누락이 런타임 버그로 이어지고, 도메인 모델이 외부 기술 관심사에 오염될 수 있다. 컴파일 타임 안전성과 도메인 순수성을 동시에 확보할 변환 전략을 결정해야 한다.

## Decision Drivers

* adapter 레이어 경계에서 필드 누락을 컴파일 시점에 감지해야 한다
* domain/application 레이어의 순수 Java 원칙을 유지해야 한다 (외부 라이브러리 금지)
* Java Record DTO와 자연스럽게 호환되어야 한다 (coding-style.md: DTO는 record 권장)
* Lombok annotation processor와 공존해야 한다
* 보일러플레이트를 최소화하여 생산성을 유지해야 한다

## Considered Options

* MapStruct Only
* 수동 변환 (static factory / 생성자)
* 하이브리드 (MapStruct + static factory)

## Decision Outcome

Chosen option: "하이브리드 (MapStruct + static factory)", because adapter 레이어 경계에서는 MapStruct의 컴파일 타임 안전성을, domain 내부에서는 static factory의 순수성을 각각 취할 수 있다. 각 컨텍스트에 최적의 도구를 사용하는 것이 과도한 일관성보다 낫다.

### Consequences

* Good, because adapter에서 필드 추가/변경 시 `unmappedTargetPolicy=ERROR`로 즉시 컴파일 에러 발생
* Good, because domain/application 모듈에 MapStruct 의존성이 없어 순수 Java 원칙 유지
* Good, because Java Record를 source/target으로 모두 사용 가능 (MapStruct 1.6.x+)
* Bad, because MapStruct 의존성 및 annotation processor 설정이 필요하다
* Bad, because Lombok과 annotation processor 순서 보장을 위해 `lombok-mapstruct-binding`이 추가로 필요하다
* Neutral, because Checkstyle suppressions에 MapperImpl 제외 규칙이 이미 존재한다

### Confirmation

* `./gradlew compileJava` 성공 — 모든 Mapper 필드가 커버됨 (`unmappedTargetPolicy=ERROR`)
* `./gradlew :template-domain:test :template-application:test` — domain/application에 MapStruct import 없음 (ArchUnit 검증)
* adapter 모듈에만 `org.mapstruct` 의존성이 추가됨을 `./gradlew dependencies`로 확인

## Pros and Cons of the Options

### MapStruct Only

* Good, because 모든 변환을 일관되게 코드 생성으로 처리한다
* Good, because `unmappedTargetPolicy=ERROR`로 컴파일 타임 안전성을 확보한다
* Good, because Record source/target을 공식 지원한다 (1.6.x+)
* Bad, because domain 내부의 단순한 Command→Domain 생성조차 MapStruct 인터페이스가 필요해 과도하다
* Bad, because domain 모듈에도 MapStruct 의존성이 필요해 순수 Java 원칙을 위반한다

### 수동 변환 (static factory / 생성자)

* Good, because 외부 의존성이 없어 domain 순수성을 완벽하게 유지한다
* Good, because 단순한 변환에서 가장 직관적이다
* Bad, because 필드 추가 시 컴파일러가 누락을 잡지 못해 런타임 버그 위험이 있다
* Bad, because adapter 레이어에서 10개 이상 필드 변환 시 보일러플레이트가 과다하다

### 하이브리드 (MapStruct + static factory)

* Good, because adapter 경계에서 컴파일 타임 안전성을 확보한다
* Good, because domain/application은 순수 Java를 유지한다
* Good, because 각 컨텍스트에 최적의 도구를 사용한다
* Neutral, because 두 패턴이 공존하므로 "어디서 무엇을 쓸지"의 기준이 명확해야 한다
* Bad, because MapStruct 의존성 추가와 annotation processor 설정이 필요하다

## More Information

* 관련 rules: `.claude/rules/architecture.md` — 객체 변환 규칙 [ADR-0013]
* 관련 rules: `.claude/rules/coding-style.md` — Mapper 네이밍 규칙
* 업계 참고: Tom Hombergs, "Get Your Hands Dirty on Clean Architecture" — Two-Way Mapping 전략
* MapStruct 공식 문서: https://mapstruct.org/documentation/stable/reference/html/
* Lombok 공존 가이드: `lombok-mapstruct-binding` (org.projectlombok:lombok-mapstruct-binding)
