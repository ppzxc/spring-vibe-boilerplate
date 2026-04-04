# Testing Rules

## 테스트 종류별 전략

### 1. 아키텍처 테스트 (ArchUnit) [ADR-0004]

- 위치: `boilerplate-domain`, `boilerplate-application` 모듈
- 도구: `archunit-junit5`
- 역할: 레이어 의존성 규칙 강제
- `allowEmptyShould(true)` 필수 — 초기 빈 상태에서도 통과해야 함

```java
@ArchTest
static final ArchRule rule =
    noClasses().should().dependOnClassesThat()
        .resideInAnyPackage("org.springframework..")
        .allowEmptyShould(true)
        .as("도메인 레이어는 Spring에 의존할 수 없다");
```

**강제 규칙 목록:**

| 테스트 | 규칙 |
|--------|------|
| `DomainArchitectureTest` | Spring 금지, JPA 금지, 허용 패키지 제한 |
| `ApplicationArchitectureTest` | Spring 금지, Outbound Port interface 강제, Command/Query UseCase interface 강제, query→command port 의존 금지 |

### 2. 단위 테스트

- 도구: JUnit 5, Mockito, FixtureMonkey
- 대상: UseCase 구현체(`*Service`), 도메인 로직
- Mock: 외부 경계(Outbound Port)만 Mock, 도메인 내부 객체 Mock 금지
- FixtureMonkey로 테스트 픽스처 생성:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
MyDomain domain = fixture.giveMeOne(MyDomain.class);
```

### 3. 웹 레이어 테스트

- 도구: `@WebMvcTest`, MockMvc, `spring-security-test`
- 대상: Controller, Security 설정
- Spring Context 최소 로딩 (`@WebMvcTest`만 사용)

### 4. 통합 테스트 (Testcontainers)

- 도구: Testcontainers, WireMock
- 대상: Repository, 외부 서비스 Adapter
- 사용할 영속화 기술에 맞는 Testcontainers 모듈 선택 (`testcontainers-postgresql`, `testcontainers-mongodb` 등)

## 명명 규칙

| 종류 | 파일명 패턴 |
|------|------------|
| 단위 테스트 | `*Test.java` |
| 통합 테스트 | `*IT.java` |
| 아키텍처 테스트 | `*ArchitectureTest.java` |

## 테스트 실행 명령

```bash
# 아키텍처 테스트만
./gradlew :boilerplate-domain:test :boilerplate-application:test

# 전체 테스트
./gradlew test

# 특정 모듈
./gradlew :<module-name>:test

# 커버리지 리포트
./gradlew testCodeCoverageReport
```
