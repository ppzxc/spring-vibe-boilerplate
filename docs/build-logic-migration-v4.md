# build-recipe-plugin → build-logic 마이그레이션 가이드 (v4)

> 이 문서는 LINE의 `com.linecorp.build-recipe-plugin`을 사용하던 Gradle 멀티모듈 프로젝트를 Gradle 공식 권장 방식인 `build-logic` (Convention Plugins) 으로 마이그레이션하기 위한 가이드입니다.
>
> **버전**: v4 (변경 이력은 문서 하단 참조)

## 마이그레이션 목표

- `gradle.properties`의 `type=...` / `label=...` 기반 설정을 → 각 모듈이 명시적으로 convention plugin을 선언하는 방식으로 전환
- 루트 `build.gradle(.kts)`의 `configure(byTypePrefix(...))` 블록을 → 각 모듈의 plugin 적용으로 분산
- `com.linecorp.build-recipe-plugin` 의존성 제거
- 빌드 캐시 효율 향상 (변경된 convention만 영향받는 모듈 재빌드)

---

## 사전 점검 (반드시 먼저 수행)

마이그레이션을 시작하기 전 아래 항목을 확인하고 결과를 출력하세요.

### 1단계: 프로젝트 현황 파악

다음 명령으로 프로젝트 구조를 파악합니다:

```bash
# 루트 빌드 파일 확인
ls -la settings.gradle* build.gradle*

# 모든 서브 모듈의 빌드 파일 위치 확인
find . -name "build.gradle*" -not -path "*/build/*" -not -path "*/.gradle/*"

# gradle.properties 확인 (type/label 키 추출)
find . -name "gradle.properties" -not -path "*/build/*" | xargs grep -l -E "^(type|label)=" 2>/dev/null
```

### 2단계: 현재 build-recipe-plugin 사용 현황 정리

다음 정보를 추출해서 표로 정리하세요:

| 항목 | 추출 방법 |
|---|---|
| 모듈 목록 | `settings.gradle(.kts)`의 `include(...)` |
| 각 모듈의 type | 각 모듈 디렉터리의 `gradle.properties`에서 `type=` 값 |
| 각 모듈의 label | 각 모듈 디렉터리의 `gradle.properties`에서 `label=` 값 |
| 적용 중인 configure 블록 | 루트 `build.gradle(.kts)`의 `configure(byTypePrefix(...))`, `configure(byLabel(...))`, `configureByType(...)` 등 |
| Kotlin DSL 여부 | `build.gradle.kts`인지 `build.gradle`인지 |

**예시 출력 형태:**
```
모듈 분석 결과:
- coffee:api:client → type=java-boot-lib
- coffee:api:protocol → type=java-lib
- coffee:api:server → type=java-boot-application
- juice:api:client → type=java-boot-lib
...

configure 블록 분석 결과:
- byTypePrefix("java") → apply java
- byTypePrefix("java") and byTypeSuffix("lib") → apply java-library
- byTypePrefix("java-boot") → apply org.springframework.boot
...
```

### 3단계: type/label 별 convention plugin 매핑 설계

추출한 type/label 조합을 그룹화해서 convention plugin으로 매핑합니다.

**매핑 원칙:**
- 공통으로 적용되는 설정 → 베이스 convention (예: `java-base-conventions`)
- 특정 type prefix에만 적용되는 설정 → 별도 convention (예: `spring-boot-conventions`)
- 베이스 convention은 다른 convention의 `plugins` 블록에서 `id("...")` 로 참조

**매핑 예시:**
```
byTypePrefix("java") and byTypeSuffix("lib")  → java-lib-conventions
byTypePrefix("java-boot") + java-application  → spring-boot-app-conventions
byTypePrefix("java-boot") + java-library      → spring-boot-lib-conventions
byTypePrefix("kotlin")                        → kotlin-conventions
```

이 매핑 결과를 사용자에게 보여주고 **확인을 받은 후** 다음 단계로 진행하세요.

---

## 마이그레이션 단계

### Step 1: build-logic 디렉터리 생성

```
project-root/
└── build-logic/
    ├── settings.gradle.kts        ← 새로 생성
    └── conventions/                ← 단일 서브프로젝트로 시작 (필요시 분리)
        ├── build.gradle.kts        ← 새로 생성
        └── src/main/kotlin/        ← convention plugin 파일들이 들어갈 위치
```

> **분리 전략**: 처음에는 `conventions/` 단일 서브프로젝트로 시작합니다. convention plugin이 5개 이상으로 늘어나거나, 일부 convention만 자주 바뀌는 경우 `java-conventions/`, `spring-conventions/` 등으로 분리합니다.

### Step 2: `build-logic/settings.gradle.kts` 생성

`build-logic`은 독립된 Gradle 빌드이므로, 자체적으로 **플러그인 저장소(`pluginManagement.repositories`)**, **의존성 저장소(`dependencyResolutionManagement.repositories`)**, **Version Catalog**를 모두 선언해야 합니다.

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()  // ← pluginMarker artifact 해석에 필요
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include("conventions")
```

#### 각 블록의 역할

| 블록 | 역할 |
|---|---|
| `pluginManagement.repositories` | `kotlin-dsl`, `typesafe-conventions` 등 build-logic 자체가 적용하는 **Gradle 플러그인**을 해석 |
| `dependencyResolutionManagement.repositories` | `pluginMarker(libs.plugins.spring.boot)` 같은 **dependency**를 해석 (반드시 `gradlePluginPortal()` 포함 필요) |
| `versionCatalogs` | 메인 프로젝트의 `gradle/libs.versions.toml`을 build-logic에서도 사용 가능하게 등록 |

#### 왜 `dependencyResolutionManagement`에 `gradlePluginPortal()`이 필요한가

`build-logic/conventions/build.gradle.kts`의 `dependencies` 블록에서 사용하는 `pluginMarker(libs.plugins.spring.boot)` 같은 코드는 **plugin marker artifact를 일반 dependency로 해석**합니다. 이 artifact는 Gradle Plugin Portal에 게시되어 있으므로 의존성 저장소 목록에 반드시 포함되어야 합니다.

#### 왜 모듈의 `build.gradle.kts`가 아니라 `settings`에 선언하는가

Gradle 공식 Best Practices가 "Repositories는 프로젝트 정의의 일부가 아니라 글로벌 빌드 로직의 일부이므로 settings가 더 적절한 위치"라고 명시합니다. `conventions/build.gradle.kts`에 `repositories` 블록을 두면 동작은 하지만, settings에 중앙화하는 것이 표준입니다.

> **Version Catalog 추가 사용**: `typesafe-conventions` 플러그인을 사용할 경우 위 코드에 plugins 블록 하나만 더 추가하면 됩니다. 자세한 내용은 아래 **Step 3-1** 참조.

### Step 3: `build-logic/conventions/build.gradle.kts` 생성

#### 방식 A: typesafe-conventions 미사용 (문자열 기반)

```kotlin
plugins {
    `kotlin-dsl`
}

// repositories 블록을 여기에 두지 않음 — Step 2의 settings.gradle.kts에서 중앙 관리

dependencies {
    // 기존 build-recipe-plugin 환경에서 사용하던 외부 플러그인을 여기에 추가
    // 좌표 형식: "{group}:{plugin-id}.gradle.plugin:{version}" (Plugin Marker 패턴)
    // 또는 실제 jar의 좌표 직접 지정
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.4")
    // implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
}
```

#### 방식 B: typesafe-conventions 사용 (권장 — Step 3-1 참조)

`typesafe-conventions` 플러그인이 제공하는 `pluginMarker()` 함수를 사용하면 Version Catalog의 `[plugins]` 섹션을 그대로 활용할 수 있어 깔끔합니다:

```kotlin
import dev.panuszewski.gradle.pluginMarker

plugins {
    `kotlin-dsl`
}

// repositories 블록을 여기에 두지 않음 — Step 2의 settings.gradle.kts에서 중앙 관리

dependencies {
    // libs.versions.toml의 [plugins] 섹션 alias를 plugin marker artifact로 변환
    implementation(pluginMarker(libs.plugins.org.springframework.boot))
    implementation(pluginMarker(libs.plugins.io.spring.dependency.management))
}
```

> **`pluginMarker()` 동작 원리**: Gradle Plugin Portal은 모든 플러그인에 대해 **`{pluginId}:{pluginId}.gradle.plugin:{version}`** 패턴의 marker artifact를 자동 게시합니다. `pluginMarker()`는 Version Catalog의 plugin alias를 이 marker artifact 좌표로 변환해주는 헬퍼 함수입니다. 이를 통해 실제 jar의 group/artifact를 모르더라도 plugin ID만으로 의존성을 선언할 수 있습니다.

> **alias 이름 매핑 규칙**: Version Catalog에서 `org.springframework.boot` 같은 plugin ID는 `libs.plugins.org.springframework.boot`처럼 점(.)이 그대로 유지된 형태로 접근합니다. (kebab-case alias라면 `libs.plugins.spring.boot` 형태)

> **중요**: 기존 루트 `build.gradle(.kts)`의 `buildscript { dependencies { classpath(...) } }`나 `plugins { id(...) version "..." apply false }`에서 사용하던 모든 외부 Gradle 플러그인을 여기 `dependencies`에 추가해야 합니다. 그래야 convention plugin 내부에서 `id("org.springframework.boot")` 또는 `alias(libs.plugins.spring.boot)` 같은 호출이 가능합니다.

> **❌ 안티패턴 — 문자열 보간으로 catalog version만 가져오기**:
> ```kotlin
> // 이렇게 하지 마세요 — typesafe-conventions의 장점을 활용하지 못함
> implementation("org.springframework.boot:spring-boot-gradle-plugin:${libs.versions.org.springframework.boot.get()}")
> ```
> typesafe-conventions를 적용했다면 위 코드 대신 방식 B의 `pluginMarker()` 방식을 사용하세요.

#### Step 3-1: Version Catalog와 `typesafe-conventions` 플러그인 적용

Gradle은 기본적으로 precompiled script plugin(`*.gradle.kts` 형태의 convention plugin) 안에서 `libs.springBoot.web` 같은 **타입세이프 접근자를 직접 지원하지 않습니다.** ([Gradle 이슈 #15383](https://github.com/gradle/gradle/issues/15383) — 2020년부터 미해결)

기본 Gradle만 사용할 경우 이런 문자열 기반 코드를 써야 합니다:

```kotlin
// 타입세이프 접근자 없이 — 못생기고 오타에 취약
val libs = versionCatalogs.named("libs")

dependencies {
    "implementation"(platform(libs.findLibrary("spring-boot-bom").get()))
    "implementation"(libs.findLibrary("spring-boot-starter-web").get())
}
```

**[`typesafe-conventions-gradle-plugin`](https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin)** 을 적용하면 일반 모듈처럼 타입세이프 접근자를 그대로 사용할 수 있습니다:

```kotlin
// typesafe-conventions 적용 후 — 깔끔
plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)
}
```

##### ⚠️ 중요: 이것은 **settings 플러그인**입니다

`typesafe-conventions`는 `kotlin-dsl`과 함께 `build.gradle.kts`에 적용하는 플러그인이 아니라 **settings.gradle.kts에 적용하는 settings 플러그인**입니다. README에 명시되어 있습니다: *"It's a settings plugin (not project plugin) so apply it in settings.gradle.kts!"*

##### 적용 방법

**1. `build-logic/settings.gradle.kts`에 plugins 블록 추가:**

Step 2에서 만든 `build-logic/settings.gradle.kts`에 `plugins` 블록만 추가하면 됩니다 (다른 블록은 그대로):

```kotlin
plugins {
    // settings 플러그인 — 반드시 settings.gradle.kts에서 적용
    id("dev.panuszewski.typesafe-conventions") version "0.10.1"
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include("conventions")
```

> **블록 순서 주의**: Kotlin DSL에서는 `plugins { }` → `pluginManagement { }` → `dependencyResolutionManagement { }` → `rootProject.name` → `include(...)` 순서를 권장합니다.

**2. `build-logic/conventions/build.gradle.kts`는 변경 없음** (Step 3 코드 그대로):

```kotlin
plugins {
    `kotlin-dsl`
    // typesafe-conventions를 여기에 추가하지 않습니다 — settings에 이미 적용됨
}
```

**3. 이제 convention plugin 안에서 타입세이프 접근자 사용 가능:**

```kotlin
// build-logic/conventions/src/main/kotlin/spring-boot-conventions.gradle.kts
plugins {
    id("java-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
}
```

##### 주의사항

- **버전 확인**: 작업 시점에 [Gradle Plugin Portal](https://plugins.gradle.org/plugin/dev.panuszewski.typesafe-conventions)에서 최신 버전을 확인하세요. 위 예시의 `0.10.1`은 작성 시점 기준입니다.
- **settings 플러그인을 build.gradle.kts에 적용 금지**: `kotlin-dsl` 옆에 같이 넣으면 동작하지 않습니다. Step 3 코드의 plugins 블록은 `kotlin-dsl`만 둡니다.
- **언젠가 제거될 의존성**: 이 플러그인의 README에는 *"Gradle이 이슈를 해결하면 이 플러그인은 더 이상 필요 없게 될 것"* 이라고 명시되어 있습니다. Gradle이 공식 지원을 추가하면 단순히 settings.gradle.kts의 plugins 블록과 dependencyResolutionManagement만 정리하면 되도록 작성하세요.

### Step 4: convention plugin 파일 작성

`build-logic/conventions/src/main/kotlin/` 아래에 `.gradle.kts` 파일을 작성합니다. **파일명이 곧 플러그인 ID** 입니다.

#### 작성 규칙

- 파일명: 케밥 케이스 권장 (`java-lib-conventions.gradle.kts` → ID `java-lib-conventions`)
- 패키지 사용 시 ID에 prefix 추가됨 (`com.myorg.java-lib-conventions`)
- 다른 convention plugin을 `plugins { id("...") }` 블록에서 적용 가능 → 계층 구조 구성

#### 변환 매핑 규칙

기존 `configure(...)` 블록의 내용을 그대로 convention plugin 파일로 옮깁니다.

**기존 (build-recipe-plugin):**
```kotlin
configure(byTypePrefix("java")) {
    apply(plugin = "java")
}

configure(byTypePrefix("java") and byTypeSuffix("lib")) {
    apply(plugin = "java-library")
}

configure(byTypePrefix("java-boot")) {
    apply(plugin = "org.springframework.boot")
}
```

**변환 후 (build-logic):**

`build-logic/conventions/src/main/kotlin/java-base-conventions.gradle.kts`:
```kotlin
plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)  // 기존 설정 유지
    }
}

repositories {
    mavenCentral()
}
```

`build-logic/conventions/src/main/kotlin/java-lib-conventions.gradle.kts`:
```kotlin
plugins {
    id("java-base-conventions")
    `java-library`
}
```

`build-logic/conventions/src/main/kotlin/spring-boot-app-conventions.gradle.kts` (typesafe-conventions 적용 시):
```kotlin
plugins {
    id("java-base-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

// 기존 byTypePrefix("java-boot") 블록의 모든 설정을 여기로
```

`build-logic/conventions/src/main/kotlin/spring-boot-app-conventions.gradle.kts` (typesafe-conventions 미적용 시):
```kotlin
plugins {
    id("java-base-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// 기존 byTypePrefix("java-boot") 블록의 모든 설정을 여기로
```

### Step 5: 루트 `settings.gradle.kts` 수정

기존:
```kotlin
plugins {
    id("com.linecorp.build-recipe-plugin") version "1.0.1"
}

include("module-a", "module-b", ...)
```

변경 후:
```kotlin
pluginManagement {
    includeBuild("build-logic")  // ← 핵심: pluginManagement 블록 안에

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "..."  // 기존 이름 유지

include("module-a", "module-b", ...)
```

> **주의**: `includeBuild()`는 반드시 `pluginManagement` 블록 **안에** 위치해야 합니다. 바깥에 두면 모듈에서 `id("java-lib-conventions")` 호출 시 플러그인을 찾지 못합니다.

### Step 6: 루트 `build.gradle(.kts)` 수정

기존의 다음 요소들을 모두 제거합니다:
- `import com.linecorp.support.project.multi.recipe.*` 등 build-recipe-plugin import
- `configure(byTypePrefix(...))`, `configure(byLabel(...))` 블록 전체
- `id("com.linecorp.build-recipe-plugin")` 플러그인 적용

루트 `build.gradle(.kts)`에는 다음 역할만 남깁니다:

1. **전체 프로젝트 공통 정보** (group, version)
2. **convention plugin을 사용하지 않는 모듈에서 직접 적용할 외부 플러그인의 버전 등록** (`apply false` 패턴)

```kotlin
plugins {
    // convention plugin 안에서만 사용하는 플러그인은 여기에 등록할 필요 없음
    // (build-logic/conventions/build.gradle.kts의 dependencies에 등록되어 있음)

    // 일부 모듈이 convention plugin을 거치지 않고 직접 사용할 외부 플러그인만 등록
    // typesafe-conventions 미적용 시:
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false

    // typesafe-conventions 적용 + Version Catalog 사용 시 (권장):
    // alias(libs.plugins.spring.boot) apply false
    // alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "com.example"
    version = "1.0.0"
}
```

#### `apply false` 패턴이 필요한 이유

서브모듈에서 `id("org.springframework.boot")` 처럼 **버전 없이** 플러그인을 사용하려면, Gradle이 그 플러그인의 좌표(group:artifact:version)를 알아야 합니다. 좌표를 알려주는 방법은 두 가지입니다:

| 방법 | 위치 | 사용 가능 범위 |
|---|---|---|
| `apply false` | 루트 `build.gradle.kts`의 plugins 블록 | 모든 서브모듈 (직접 적용) |
| `dependencies { implementation(...) }` | `build-logic/conventions/build.gradle.kts` | convention plugin 내부에서만 |

**판단 기준:**
- convention plugin 안에서만 사용 → `build-logic`의 `dependencies`에만 등록
- 일부 모듈이 convention plugin 없이 직접 적용 → 루트의 `plugins` 블록에 `apply false`로 등록
- 둘 다 가능성이 있음 → 양쪽 모두 등록 (이때 버전이 일치해야 함, Version Catalog로 일원화 권장)

> **주의**: `apply false`를 빼먹으면 루트 프로젝트에 플러그인이 적용되어 `bootJar` 같은 태스크가 루트에서 실행되는 사고가 발생합니다. 반드시 `apply false`를 명시하세요.

### Step 7: 각 모듈의 `build.gradle.kts` 작성/수정

각 모듈에 build 파일이 없었다면 새로 만들고, 있었다면 plugins 블록을 수정합니다.

**예시 — `coffee/api/protocol/build.gradle.kts` (type=java-lib):**
```kotlin
plugins {
    id("java-lib-conventions")
}

dependencies {
    // 모듈 고유 의존성만
}
```

**예시 — `coffee/api/server/build.gradle.kts` (type=java-boot-application):**
```kotlin
plugins {
    id("spring-boot-app-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

### Step 8: `gradle.properties` 정리

각 모듈의 `gradle.properties`에서 `type=`, `label=` 라인을 제거합니다. 다른 속성이 있으면 유지합니다.

**주의**: 루트 `gradle.properties`의 Gradle 옵션(`org.gradle.jvmargs`, `org.gradle.parallel` 등)은 절대 건드리지 않습니다.

### Step 9: build-recipe-plugin 의존성 완전 제거

다음 위치에서 `com.linecorp.build-recipe-plugin` 관련 항목이 남아있는지 검색하고 모두 제거합니다:

```bash
grep -rn "build-recipe-plugin" . --include="*.gradle" --include="*.gradle.kts" --include="*.properties"
grep -rn "com.linecorp.support.project.multi.recipe" . --include="*.gradle" --include="*.gradle.kts" --include="*.kt"
```

---

## 검증

### 빌드 테스트

```bash
# 1. clean 후 전체 빌드
./gradlew clean build --no-build-cache

# 2. 캐시 활성화 상태로 한 번 더 (캐시 동작 확인)
./gradlew clean build

# 3. 특정 모듈만 빌드해서 의존성 그래프가 올바른지 확인
./gradlew :module-name:build
```

### 기능 검증 체크리스트

- [ ] 모든 모듈이 빌드 성공
- [ ] 각 모듈의 컴파일 결과물이 기존과 동일 (jar 안의 클래스, 매니페스트 등)
- [ ] Spring Boot 애플리케이션 모듈은 `bootJar` 태스크가 정상 동작
- [ ] 라이브러리 모듈은 `jar` 태스크가 정상 동작
- [ ] 테스트 태스크가 정상 동작
- [ ] `./gradlew projects`로 모듈 구조가 기존과 동일한지 확인

### 캐시 효과 검증

```bash
# 한 번 빌드해서 캐시 채우기
./gradlew build

# 특정 convention plugin만 수정 (예: spring-boot-app-conventions.gradle.kts)
# 그 다음 빌드 시 영향받는 모듈만 재빌드되는지 확인
./gradlew build --info | grep -E "(UP-TO-DATE|FROM-CACHE|Task)"
```

---

## 트러블슈팅

### 문제 1: `Plugin with id 'xxx-conventions' not found`

**원인**: `settings.gradle.kts`에서 `includeBuild("build-logic")`이 `pluginManagement` 블록 바깥에 있거나 누락됨.

**해결**: Step 5의 구조대로 `pluginManagement { includeBuild("build-logic") }` 위치 확인.

### 문제 2: convention plugin 안에서 `id("org.springframework.boot")` 호출 시 에러

**원인**: `build-logic/conventions/build.gradle.kts`의 `dependencies` 블록에 해당 플러그인이 `implementation`으로 등록되지 않음.

**해결**: Step 3의 dependencies 블록에 추가:
```kotlin
implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
```

### 문제 3: 일부 모듈만 type 속성이 없어서 어떤 convention을 적용할지 모호함

**해결**: 사용자에게 해당 모듈의 의도를 묻고, 가장 가까운 convention을 적용하거나 신규 convention을 만듭니다. 절대 임의로 추측하지 마세요.

### 문제 4: 기존 `subprojects { ... }` 블록에 다른 설정도 있었음

**해결**: 그 설정도 모두 적절한 convention plugin으로 옮겨야 합니다. `subprojects { ... }`를 그대로 남겨두면 build-recipe-plugin 시절의 매직이 사라진 자리에 일반 설정이 충돌할 수 있습니다.

### 문제 5: `byLabel` 기반 조합 설정이 복잡함

**해결**: label은 보통 cross-cutting concern (예: "deployable", "internal")입니다. label별로 별도 convention plugin을 만들고, 해당하는 모듈에서 두 개 이상의 convention을 함께 적용하는 방식으로 처리합니다:

```kotlin
plugins {
    id("java-lib-conventions")
    id("deployable-conventions")  // label=deployable에 해당하던 설정
}
```

### 문제 6: `Unresolved reference: libs` (convention plugin 안에서 Version Catalog 미인식)

**원인 가능성 1**: `typesafe-conventions` 플러그인을 `build-logic/conventions/build.gradle.kts`의 plugins 블록에 적용함.

**해결**: `typesafe-conventions`는 **settings 플러그인**입니다. `build-logic/settings.gradle.kts`의 plugins 블록으로 옮기세요:
```kotlin
// build-logic/settings.gradle.kts
plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.10.1"
}
```

**원인 가능성 2**: `build-logic/settings.gradle.kts`에서 catalog를 명시적으로 등록하지 않음.

**해결**: Step 3-1대로 `dependencyResolutionManagement { versionCatalogs { ... } }` 블록 추가:
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

### 문제 7: typesafe-conventions를 적용하지 않고 catalog를 쓰려고 할 때

**원인**: 기본 Gradle은 precompiled script plugin에서 `libs.*` 타입세이프 접근자를 지원하지 않음.

**해결 옵션**:
- (권장) Step 3-1에 따라 `typesafe-conventions` 플러그인 적용
- 또는 문자열 기반 API 사용:
  ```kotlin
  val libs = versionCatalogs.named("libs")
  dependencies {
      "implementation"(libs.findLibrary("spring-boot-starter-web").get())
  }
  ```

### 문제 8: 모듈에서 `id("org.springframework.boot")` 사용 시 `Plugin not found` 또는 `version is required`

**원인**: 서브모듈이 convention plugin을 거치지 않고 직접 외부 플러그인을 사용하는데, 루트 `build.gradle.kts`에 해당 플러그인이 등록되어 있지 않음.

**해결**: Step 6대로 루트 `build.gradle.kts`의 plugins 블록에 `apply false`로 등록:
```kotlin
plugins {
    id("org.springframework.boot") version "3.2.5" apply false
}
```

또는 그 모듈에 convention plugin을 적용하도록 변경:
```kotlin
// 모듈 build.gradle.kts
plugins {
    id("spring-boot-app-conventions")  // build-logic의 convention 사용
}
```

### 문제 9: 루트 프로젝트에서 `bootJar` 등 예상치 못한 태스크가 실행됨

**원인**: 루트 `build.gradle.kts`의 plugins 블록에 `apply false`를 빼먹어 루트 프로젝트에까지 플러그인이 적용됨.

**해결**: 모든 외부 플러그인 등록 시 반드시 `apply false` 명시:
```kotlin
plugins {
    id("org.springframework.boot") version "3.2.5" apply false  // ← 필수
}
```

### 문제 10: build-logic의 build.gradle.kts에서 catalog version만 문자열 보간으로 사용

**증상**: 다음과 같이 코드가 장황하고 일관성이 없음:
```kotlin
implementation("org.springframework.boot:spring-boot-gradle-plugin:${libs.versions.org.springframework.boot.get()}")
```

**원인**: typesafe-conventions를 적용했지만 `pluginMarker()` 헬퍼를 모르고 version 문자열만 catalog에서 가져옴. group/artifact는 여전히 하드코딩.

**해결**: Step 3 방식 B대로 `pluginMarker()` 사용:
```kotlin
import dev.panuszewski.gradle.pluginMarker

dependencies {
    implementation(pluginMarker(libs.plugins.org.springframework.boot))
    implementation(pluginMarker(libs.plugins.io.spring.dependency.management))
}
```

이렇게 하면 group/artifact 좌표를 알 필요 없이 plugin alias만으로 의존성 선언이 됩니다.

### 문제 11: `Could not find ...gradle.plugin:...` (pluginMarker artifact 해석 실패)

**증상**: `pluginMarker()`를 사용한 코드에서 다음과 같은 에러 발생:
```
Could not find org.springframework.boot:org.springframework.boot.gradle.plugin:3.2.5.
```

**원인**: `build-logic/settings.gradle.kts`의 `dependencyResolutionManagement.repositories`에 `gradlePluginPortal()`이 빠져있음. Plugin marker artifact는 Gradle Plugin Portal에 게시되어 있어 dependency 저장소 목록에 반드시 포함되어야 합니다.

**해결**: Step 2대로 `dependencyResolutionManagement.repositories`에 `gradlePluginPortal()` 추가:
```kotlin
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()  // ← 이게 빠지면 pluginMarker 해석 실패
        mavenCentral()
    }
    // ...
}
```

### 문제 12: `repositoriesMode = FAIL_ON_PROJECT_REPOS`와 `conventions/build.gradle.kts`의 `repositories` 충돌

**증상**: build-logic settings에 `RepositoriesMode.FAIL_ON_PROJECT_REPOS`를 설정했더니 다음 에러 발생:
```
Build was configured to prefer settings repositories over project repositories
but repository 'MavenRepo' was added by build file 'conventions/build.gradle.kts'
```

**원인**: `conventions/build.gradle.kts`에 `repositories { ... }` 블록이 남아있음.

**해결**: Step 3대로 `conventions/build.gradle.kts`에서 `repositories` 블록을 완전히 제거. 모든 저장소는 `build-logic/settings.gradle.kts`에서 중앙 관리.

---

## 버전 정보

이 문서는 `build-logic-migration-v4` 입니다. 주요 변경 사항:

- **v1**: 초기 가이드 (build-logic 기본 구조)
- **v2**: typesafe-conventions 플러그인 도입 (Version Catalog 타입세이프 접근자)
- **v3**: typesafe-conventions를 settings 플러그인으로 정정, `pluginMarker()` 사용 패턴 추가, 루트 `apply false` 패턴 명시
- **v4**: `build-logic/settings.gradle.kts`에 `pluginManagement.repositories` + `dependencyResolutionManagement.repositories` 중앙화, `conventions/build.gradle.kts`에서 `repositories` 블록 제거 (Gradle 공식 best practice 반영)

---

## 작업 진행 시 주의사항 (Claude Code 전용)

1. **사전 점검 결과를 먼저 사용자에게 보고**하고, 매핑 설계에 대해 **확인을 받은 후** 코드 수정을 시작할 것
2. 한 번에 모든 파일을 바꾸지 말고, **단계별로 작업하고 중간 검증**할 것
3. 기존 파일을 수정할 때는 **백업 또는 git diff 확인**을 먼저 권유할 것
4. `gradle.properties`의 Gradle 시스템 옵션(`org.gradle.*`)은 **절대 수정하지 말 것**
5. 모듈별 `build.gradle(.kts)` 신규 생성 시, 기존 `gradle.properties`의 type/label만 보고 추측하지 말고 **실제 사용된 configure 블록의 내용을 모두 확인**할 것
6. 각 단계 완료 시 `./gradlew help` 또는 `./gradlew projects`를 실행해 빌드 시스템이 깨지지 않았는지 검증할 것
7. 마이그레이션 도중 빌드가 깨졌을 때, **임시방편으로 build-recipe-plugin을 다시 적용하지 말 것**. 원인을 찾아 해결할 것
