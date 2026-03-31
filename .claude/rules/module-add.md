# Module Add Rules

## 새 모듈 추가 절차

새 모듈을 추가할 때 아래 순서를 따른다.

### 1. 디렉토리 및 기본 파일 생성

```
template/<module-name>/
  build.gradle.kts
  gradle.properties
  src/main/java/io/github/ppzxc/template/<layer>/package-info.java
```

### 2. `settings.gradle.kts` 등록

`module("template/<module-name>")` 한 줄 추가.

### 3. `build.gradle.kts` 작성

레이어별 의존성 규칙:

**template-domain 추가 시:**
```kotlin
dependencies {
    implementation(libs.org.projectlombok.lombok)
    implementation(libs.org.jspecify)
    // Spring/JPA 의존 추가 금지
}
```

**template-adapter-output-persist 패턴:**
```kotlin
dependencies {
    implementation(project(":template-application"))
    implementation(libs.org.springframework.boot.starter.data.jpa)
    implementation(libs.org.flywaydb.core)
}
```

### 4. AutoConfiguration 등록 (adapter 모듈)

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
에 AutoConfiguration 클래스 FQCN 추가.

### 5. ArchUnit 테스트 추가 (domain, application 레이어)

`DomainArchitectureTest` 또는 `ApplicationArchitectureTest` 패턴 참고.
새 레이어 규칙이 있으면 테스트 클래스 추가.

### 6. 빌드 확인

```bash
./gradlew :<module-name>:compileJava
./gradlew :<module-name>:test
```

## 모듈 간 의존 방향

```
template-boot-* (앱)
  └─ template-adapter-input-* (Inbound)
  └─ template-adapter-output-* (Outbound)
       └─ template-application (UseCase + Port)
            └─ template-domain (Domain)
            └─ template-common (Util)
```

역방향 의존 금지. domain이 application을 알면 안 된다.
