---
description: Harness Engineering 도구 설정 — build-recipe 라벨 상세, 커버리지 게이트, 코드 품질 도구
alwaysApply: true
---

# Harness Engineering Rules

빌드 도구 및 코드 품질 설정 규칙 — 항상 로드.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.

---

## 1. build-recipe-plugin 라벨 상세

linecorp build-recipe-plugin 라벨 → `configureByLabel()` 자동 적용.

### `java` 라벨 포함 도구

| 도구 | 버전 | 역할 |
|------|------|------|
| Java | 25 | toolchain 설정 |
| ErrorProne | 최신 | 컴파일 타임 버그 탐지 |
| NullAway | 최신 | Null 안전성 정적 분석 |
| Spotless | 최신 | 코드 포맷 (Google Java Format) |
| Checkstyle | 최신 | 스타일 규칙 강제 |
| ArchUnit | 1.4.1 | 패키지 의존 방향 검증 |
| Fixture Monkey | 1.1.19 | 테스트 픽스처 자동 생성 |
| JUnit 5 | Spring Boot BOM | 단위/통합 테스트 |

```kotlin
// {bc}-domain/build.gradle.kts
label("java")
label("coverage-gate")
// dependencies {} 블록 없음 — 외부 의존 제로 (D-1)
```

### `spring` 라벨 포함 의존성

```kotlin
// 자동 추가됨 (선언 불필요)
implementation("org.springframework.boot:spring-boot-starter")
implementation("org.springframework.boot:spring-boot-starter-validation")
implementation("org.springframework.boot:spring-boot-starter-json")
```

### `boot` 라벨

```kotlin
// boilerplate-boot-api/build.gradle.kts
label("java", "spring", "boot")
// BootJar 태스크 활성화
```

### `jooq` 라벨

```kotlin
// {bc}-adapter-output-persist/build.gradle.kts
label("java", "spring", "jooq")
// jOOQ Codegen + H2(스키마 추출용) + spring-boot-starter-jooq 자동 추가
```

### `mapstruct` 라벨

```kotlin
// {bc}-adapter-input-api, {bc}-adapter-output-persist 에 필요 시 추가
label("java", "spring", "mapstruct")
// MapStruct + annotationProcessor 자동 설정
```

### `coverage-gate` 라벨

```kotlin
// domain, application 모듈에 선언
label("java", "coverage-gate")
// JaCoCo LINE 커버리지 80% 미달 시 빌드 실패
```

---

## 2. Gradle 태스크 실행 순서

```
compileJava → processResources → classes
    → test (JUnit 5)
        → jacocoTestReport → jacocoTestCoverageVerification (coverage-gate)
    → spotlessCheck → checkstyleMain
    → archUnitTest
```

- MUST: `./gradlew check` 로컬 실행 후 커밋한다.
- MUST NOT: `--no-daemon` 없이 CI에서 Gradle 실행한다 (메모리 누수 방지).

---

## 3. ArchUnit 검증 규칙 위치

```
boilerplate-boot-api/src/test/java/.../architecture/
    ArchitectureTest.java          # 의존 방향 + 금지 import
    ModulithStructureTest.java     # ApplicationModules.verify()
```

- MUST: ArchUnit 테스트는 `boilerplate-boot-api` 모듈에 위치한다.
- MUST: `ModulithStructureTest.verify()` 를 CI에서 항상 실행한다.

### ArchUnit 핵심 규칙 목록

```java
// domain → Spring 금지 (D-1)
noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
    .check(classes);

// domain → UUID.randomUUID() 직접 호출 금지 (ADR-0011)
noClasses().that().resideInAPackage("..domain..")
    .should().callMethod(UUID.class, "randomUUID")
    .check(classes);

// application → Spring 금지 (A-1)
noClasses().that().resideInAPackage("..application..")
    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
    .check(classes);

// application → @Transactional 금지 (A-4) — 클래스 레벨
noClasses().that().resideInAPackage("..application..")
    .should().beAnnotatedWith(Transactional.class)
    .check(classes);

// application → @Transactional 금지 (A-4) — 메서드 레벨 (Tier 1 완전 보장)
noMembers().that().areDeclaredInClassesThat().resideInAPackage("..application..")
    .should().beAnnotatedWith(Transactional.class)
    .check(classes);

// adapter-input → domain 직접 참조 금지 (AD-1)
noClasses().that().resideInAPackage("..adapter.input..")
    .should().dependOnClassesThat().resideInAPackage("..domain.model..")
    .check(classes);
```

---

## 4. Spotless (코드 포맷)

```kotlin
// build-recipe-plugin이 java 라벨에서 자동 적용
spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
    }
}
```

- MUST: 커밋 전 `./gradlew spotlessApply` 실행하거나 IDE 저장 시 자동 포맷.
- MUST NOT: `spotlessCheck` 실패를 무시하고 PR을 병합한다.

---

## 5. NullAway + ErrorProne 설정

```kotlin
// build-recipe-plugin java 라벨 자동 설정
tasks.withType<JavaCompile> {
    options.errorprone {
        option("NullAway:AnnotatedPackages", "io.github.ppzxc.boilerplate")
    }
}
```

- MUST: NullAway 경고를 오류로 처리한다. 억제(`@SuppressWarnings`)는 ADR 근거 필수.
- MUST: domain/application 계층은 JSpecify 없이 `Objects.requireNonNull`로 Null-Safety 보장.
- MAY: adapter/configuration 계층에서 `@Nullable`/`@NonNull` 어노테이션 사용.

---

## 6. 커버리지 게이트 기준

| 모듈 | 라벨 | 커버리지 기준 | 측정 대상 |
|------|------|-------------|----------|
| `{bc}-domain` | `coverage-gate` | LINE 80% | 모든 Domain 클래스 |
| `{bc}-application` | `coverage-gate` | LINE 80% | 모든 Service 클래스 |
| `{bc}-adapter-*` | — | 커버리지 게이트 없음 | 통합 테스트로 보완 |

- MUST NOT: `{bc}-domain`, `{bc}-application`에서 커버리지 80% 미달 상태로 PR을 병합한다.

---

## fallback 지시문

> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> `docs/decisions/` 디렉토리에서 관련 ADR을 직접 읽어
> 결정의 배경을 파악한 후 최적의 대안을 제안하라.
