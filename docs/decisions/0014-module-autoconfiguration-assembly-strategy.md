---
status: accepted
date: 2026-04-02
decision-makers: ppzxc
---

# 모듈 자동 조립 전략: 모듈별 AutoConfiguration 자체 등록

## Context and Problem Statement

Hexagonal Architecture(ADR-0001, ADR-0002)에 따라 8개 모듈이 독립적으로 분리되어 있다.
현재 adapter 모듈들은 `@SpringBootApplication`에 내장된 `@ComponentScan`의 암묵적 스캔 범위에 의존하여 Bean이 등록된다.
이 방식은 Boot 모듈과 각 adapter 모듈 사이에 암묵적 결합을 만들며, 모듈 경계와 조립 책임이 불명확해진다.
각 모듈이 자신의 Bean 등록 책임을 명시적으로 소유하는 조립 전략이 필요하다.

## Decision Drivers

* 모듈 경계 명확화 — 각 모듈이 자신의 Bean 등록을 스스로 선언
* Boot 모듈과 adapter 모듈 간 암묵적 결합 제거
* Spring 공식 AutoConfiguration 관행 준수 ("never the target of component scanning")
* 이중 로딩 방지 안전장치 확보
* 기존 `template-application-autoconfiguration` 패턴과의 일관성 유지 (ADR-0012)

## Considered Options

* Option 1: Boot 모듈 중앙 `@Configuration` 조립 (Buckpal 스타일)
* Option 2: 모듈별 AutoConfiguration 자체 등록 (채택)
* Option 3: `@ComponentScan` 기반 전면 자동 스캔
* Option 4: Spring Modulith

## Decision Outcome

Chosen option: "Option 2: 모듈별 AutoConfiguration 자체 등록", because 각 모듈이 자신의 Bean 등록 책임을 `AutoConfiguration.imports` 파일로 명시적으로 선언하여 Boot 모듈과의 암묵적 결합을 제거하고, 이미 UseCase 조립에 이 패턴을 채택한 `template-application-autoconfiguration`(ADR-0012)과 일관성을 유지한다.

### Consequences

* Good, because 각 모듈이 자신의 Bean 등록 책임을 명시적으로 소유한다
* Good, because Boot 모듈이 adapter 구현 세부사항을 알 필요가 없다
* Good, because `AutoConfigurationExcludeFilter`가 `AutoConfiguration.imports`에 등록된 클래스를 ComponentScan에서 자동 제외하여 이중 로딩을 방지한다
* Good, because application-autoconfiguration(ADR-0012)과 동일한 조립 패턴으로 일관성을 확보한다
* Bad, because Bean 등록 위치가 분산되어 디버깅이 어렵다 — `--debug` 플래그 또는 `/actuator/conditions` 엔드포인트로 확인 필요
* Bad, because AutoConfiguration 간 의존 관계가 있을 경우 `@AutoConfigureBefore` / `@AutoConfigureAfter`를 명시해야 한다
* Bad, because 슬라이스 테스트(`@WebMvcTest`, `@DataJpaTest`)에서 불필요한 AutoConfiguration이 로딩될 수 있어 `@ImportAutoConfiguration(exclude=...)` 관리가 필요하다
* Bad, because AutoConfiguration은 원래 라이브러리용 메커니즘으로 설계되었으며, 단일 애플리케이션 내부 모듈에 적용하는 것은 과도한 추상화로 볼 수 있다

### Forbidden Patterns

* Boot 모듈에서 `@ComponentScan` 커스터마이징 금지 — `@SpringBootApplication`의 기본 `AutoConfigurationExcludeFilter`를 파괴하여 이중 로딩을 유발한다
* Boot 모듈에서 `@Bean` 직접 등록 금지 — 모듈 자체 조립 원칙 위반, Boot 모듈에 구현체 지식이 집중된다
* `AutoConfiguration.imports` 미등록 상태에서 `@AutoConfiguration` 사용 금지 — 순서 제어 없이 ComponentScan에 의해 로딩될 수 있다
* adapter 모듈에서 `@Component`/`@Service`/`@Repository` 스테레오타입 사용 금지 — ComponentScan 의존을 재도입한다

### Confirmation

* `./gradlew compileJava`가 성공해야 한다 — `AutoConfiguration.imports` 등록 누락 시 Bean 없음 오류로 실패
* `ApplicationArchitectureTest`와 `DomainArchitectureTest`가 통과해야 한다 — 레이어 의존성 규칙 위반 없음을 보장
* `/actuator/conditions` 엔드포인트에서 각 AutoConfiguration 클래스가 `positiveMatches`에 나타나야 한다
* Boot 모듈에 커스텀 `@ComponentScan`이 없어야 한다 — `AutoConfigurationExcludeFilter` 보호 유지 확인

## Pros and Cons of the Options

### Option 1: Boot 모듈 중앙 `@Configuration` 조립 (Buckpal 스타일)

Boot 모듈에 `@Configuration` 클래스를 두고 모든 adapter Bean을 한 곳에서 `@Bean`으로 명시 등록한다.

* Good, because Bean 등록 위치가 한 곳에 집중되어 추적이 쉽다
* Good, because Spring 공식 문서 관행에서 벗어나지 않는다
* Neutral, because Tom Hombergs의 *Get Your Hands Dirty on Clean Architecture*에서 채택하는 방식이다
* Bad, because Boot 모듈이 모든 adapter 구현 세부사항을 알아야 하므로 Boot ↔ adapter 결합이 명시적으로 노출된다
* Bad, because 새 adapter Bean 추가 시 Boot 모듈을 반드시 수정해야 한다

### Option 2: 모듈별 AutoConfiguration 자체 등록 (채택)

각 모듈에 `@AutoConfiguration` 클래스와 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일을 두어 자체적으로 Bean을 등록한다.

AutoConfiguration 패키지 격리 규칙:
- 현재: `io.github.ppzxc.template.application.autoconfiguration`
- 변경 후: `io.github.ppzxc.template.autoconfigure.*`
- application 모듈: `io.github.ppzxc.template.autoconfigure.application`
- adapter 모듈 패턴: `io.github.ppzxc.template.autoconfigure.adapter.{direction}.{type}`

패키지를 격리하는 이유는 Spring 공식 문서 권고("AutoConfiguration 클래스는 component scanning의 대상이 되어서는 안 된다")를 명시적으로 준수하기 위해서다. `@SpringBootApplication`의 `AutoConfigurationExcludeFilter`가 이중 로딩을 자동 방지하지만, 커스텀 `@ComponentScan` 선언 시 이 필터가 사라지므로 패키지 격리로 안전장치를 이중화한다.

* Good, because 각 모듈이 Bean 등록 책임을 자체 소유하여 모듈 경계가 명확하다
* Good, because Boot 모듈은 adapter 구현을 알 필요 없이 AutoConfiguration 메커니즘으로 조립된다
* Good, because `AutoConfigurationExcludeFilter` + 패키지 격리의 이중 안전장치로 이중 로딩을 방지한다
* Neutral, because UseCase Bean 추가마다 `@Bean` 등록이 필요하다 (Option 1과 동일한 수준)
* Bad, because 디버깅 시 Bean 출처 파악에 추가 단계가 필요하다
* Bad, because 단일 앱 내부에 라이브러리용 메커니즘을 적용하는 과도한 추상화다

### Option 3: `@ComponentScan` 기반 전면 자동 스캔

`@SpringBootApplication`의 기본 `@ComponentScan` 범위(`io.github.ppzxc.template`)에서 `@Component`, `@Service`, `@Repository` 등의 스테레오타입을 스캔하여 자동 등록한다.

* Good, because 설정 없이 동작하여 가장 단순하다
* Bad, because application 레이어에 Spring 스테레오타입을 사용해야 하므로 ArchUnit 규칙(Spring 금지)을 위반한다
* Bad, because 모든 모듈이 Boot 모듈의 스캔 범위에 암묵적으로 의존하여 모듈 경계가 흐려진다
* Bad, because 스캔 범위 변경 시 전체 모듈에 영향을 미쳐 변경 파급 범위를 예측하기 어렵다

### Option 4: Spring Modulith

Spring Modulith를 도입하여 모듈 경계를 프레임워크 수준에서 관리한다.

* Good, because 모듈 경계와 의존성을 프레임워크가 검증한다
* Good, because 모듈 간 이벤트 기반 통신 등 고급 기능을 제공한다
* Neutral, because ArchUnit(ADR-0004)과 역할이 중복된다
* Bad, because Spring Modulith는 단일 Maven/Gradle 모듈 내의 패키지 기반 모듈을 전제로 설계되어, 이 프로젝트의 Gradle 멀티모듈 구조와 맞지 않는다
* Bad, because 현재 요구사항 대비 과도한 의존성 추가다

## More Information

* 레이어 의존성 제약 근거: [ADR-0001](0001-hexagonal-architecture-and-cqrs.md)
* 모듈 구조 근거: [ADR-0002](0002-flat-module-structure.md)
* AutoConfiguration 패턴 선례 (UseCase Bean 등록): [ADR-0012](0012-transaction-management-strategy.md)
* Spring Boot Reference — "Creating Your Own Auto-configuration": https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html
* Spring Boot Reference — `AutoConfigurationExcludeFilter`: ComponentScan에서 `AutoConfiguration.imports` 등록 클래스를 자동 제외하는 필터
