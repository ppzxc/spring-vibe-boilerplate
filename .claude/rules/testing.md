# Testing Rules

## 테스트 종류별 전략

### 1. 아키텍처 테스트 (ArchUnit)

- 위치: `template-domain`, `template-application` 모듈
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

### 2. 단위 테스트

- 도구: JUnit 5, Mockito, FixtureMonkey
- 대상: UseCase 구현체, 도메인 로직
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
- 대상: JPA Repository, 외부 서비스 Adapter
- PostgreSQL 컨테이너: `org.testcontainers:testcontainers-postgresql`

```java
@Testcontainers
class MyRepositoryIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");
}
```

## 명명 규칙

| 종류 | 파일명 패턴 |
|------|------------|
| 단위 테스트 | `*Test.java` |
| 통합 테스트 | `*IT.java` |
| 아키텍처 테스트 | `*ArchitectureTest.java` |

## 테스트 실행 명령

```bash
# 아키텍처 테스트만
./gradlew :template-domain:test :template-application:test

# 전체 테스트
./gradlew test

# 특정 모듈
./gradlew :<module-name>:test
```
