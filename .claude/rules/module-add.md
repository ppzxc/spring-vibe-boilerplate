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

**boilerplate-domain 계열:**
```kotlin
dependencies {
    // Spring/JPA 의존 추가 금지
    // 외부 라이브러리는 최소화 (java.*, jakarta.validation.* 범위 내)
}
```

**boilerplate-application 계열:**
```kotlin
dependencies {
    api(project(":boilerplate-domain"))
    // Spring 의존 추가 금지
}
```

**boilerplate-adapter-output-persist 패턴:**
```kotlin
dependencies {
    implementation(project(":boilerplate-application"))
    // 사용할 영속화 기술에 맞는 의존성 추가 (JPA, R2DBC, MongoDB 등)
}
```

**template-adapter-input-grpc 패턴 (gRPC 확장 시):**
```kotlin
// gradle.properties: label=java,spring,proto
dependencies {
    implementation(project(":boilerplate-application"))
    implementation(libs.io.grpc.netty.shaded)
    implementation(libs.io.grpc.protobuf)
    implementation(libs.io.grpc.stub)
}
```

`proto` 라벨이 Protobuf 플러그인과 코드 생성을 자동 적용한다.
`.proto` 파일은 `src/main/proto/`에 배치.

**template-boot-grpc 패턴 (gRPC 전용 서버 필요 시):**
```kotlin
// gradle.properties: label=java,spring,boot
dependencies {
    implementation(project(":boilerplate-application-autoconfiguration"))
    implementation(project(":template-adapter-input-grpc"))
    // 필요에 따라 output adapter 추가
}
```

### 4. AutoConfiguration 등록 (adapter 모듈)

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
에 AutoConfiguration 클래스 FQCN 추가.

AutoConfiguration 클래스 패키지 네이밍 규칙 [ADR-0014]:
- application 모듈: `io.github.ppzxc.boilerplate.autoconfigure.application`
- adapter 모듈: `io.github.ppzxc.boilerplate.autoconfigure.adapter.{direction}.{type}`
  - 예: `io.github.ppzxc.boilerplate.autoconfigure.adapter.input.api`
  - 예: `io.github.ppzxc.boilerplate.autoconfigure.adapter.output.persist`

### 5. ArchUnit 테스트 추가 (domain, application 레이어)

`DomainArchitectureTest` 또는 `ApplicationArchitectureTest` 패턴 참고.
새 레이어 규칙이 있으면 테스트 클래스 추가.

### 6. 빌드 확인

```bash
./gradlew :<module-name>:compileJava
./gradlew :<module-name>:test
```

## 모듈 간 의존 방향

의존성 방향 규칙은 `rules/architecture.md` 참조. 역방향 의존 금지. adapter 간 상호 의존 전면 금지.
