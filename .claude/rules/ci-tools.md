# CI Tools Rules

## 도구 역할 요약 [ADR-0005] [ADR-0006]

| 도구 | 시점 | 역할 | 자동수정 |
|------|------|------|----------|
| Spotless | pre-commit / CI | 코드 포맷 | ✓ |
| Checkstyle | pre-commit / CI | 네이밍, 금지패턴 | ✗ |
| ErrorProne | 컴파일 | 버그 패턴 탐지 | ✗ |
| NullAway | 컴파일 | Null 안전성 | ✗ |
| OpenRewrite | pre-push / CI | 코드 현대화 제안 | ✓ (`rewriteRun`) |
| Lefthook | Git hook | 로컬 품질 게이트 | — |
| JaCoCo | CI | 커버리지 집계 | — |
| ArchUnit | 테스트 시 | 아키텍처 규칙 강제 | — |

## 실행 명령

```bash
# 포맷 수정 (코드 작성 후 항상 실행)
./gradlew spotlessApply

# 포맷 검사
./gradlew spotlessCheck

# 네이밍/구조 검사
./gradlew checkstyleMain

# 전체 컴파일 (ErrorProne + NullAway 포함)
./gradlew compileJava

# 코드 현대화 미리보기
./gradlew rewriteDryRun

# 코드 현대화 적용
./gradlew rewriteRun

# 아키텍처 테스트만
./gradlew :template-domain:test :template-application:test

# 전체 테스트
./gradlew test

# 커버리지 리포트 생성
./gradlew testCodeCoverageReport
```

## Lefthook Git Hooks [ADR-0006]

설정 파일: `lefthook.yml`

```
pre-commit (병렬, *.java 변경 시):
  ├── spotlessCheck
  ├── checkstyleMain
  └── compileJava

pre-push:
  └── rewriteDryRun
```

Lefthook 설치 후 `./gradlew lefthookInstall` 실행하여 hook 등록.

## Checkstyle 주요 금지 규칙

| 규칙 | 대안 | 이유 |
|------|------|------|
| `synchronized` 금지 | `ReentrantLock` | Virtual Thread 핀닝 방지 |
| `ThreadLocal` 금지 | `ScopedValue` | Virtual Thread 누수 방지 |
| 와일드카드 import 금지 | 명시적 import | 가독성 |
| 탭 금지 | 공백 | 포맷 일관성 |

설정 파일: `config/checkstyle/checkstyle.xml`
억제 파일: `config/checkstyle/suppressions.xml` (generated/, MapperImpl, 테스트 파일 제외)

## GitHub Actions Jobs [ADR-0006]

설정 파일: `.github/workflows/test.yml`

| 잡 | 트리거 | 포함 모듈 |
|----|--------|----------|
| `code-quality` | push/PR | 전체 (spotless, checkstyle, rewriteDryRun) |
| `unit-and-slice` | push/PR | domain, application, autoconfiguration, adapter-input-*, adapter-output-cache |
| `integration-test` | push/PR | adapter-output-persist, boot-api |
| `coverage-report` | unit+integration 완료 후 | 루트 (집계) |
