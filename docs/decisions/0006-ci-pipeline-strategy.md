---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# CI 파이프라인 전략: Lefthook + GitHub Actions + JaCoCo + OpenRewrite

## 배경 및 문제

코드 품질 도구(Spotless, Checkstyle, ErrorProne, ArchUnit)가 있어도 CI에 통합되지 않으면
push 이후에야 문제가 발견된다. 로컬에서 빠른 피드백을 주고, 원격에서는 전체 검증을 수행하는
다단계 파이프라인이 필요하다.

## 결정 기준

* 로컬 개발 시 빠른 피드백 (push 전 차단)
* CI에서 전체 테스트 + 커버리지 집계
* 코드 현대화 자동 제안
* 설정 복잡도 최소화

## 채택 도구

### 1. Lefthook (0.4.0) — Git Hooks 관리

**역할:** 로컬 개발 환경에서 push/commit 전 품질 검사

**선택 이유:**
* Git hooks를 버전 관리(`lefthook.yml`)로 관리
* 병렬 실행으로 빠른 피드백
* 팀/에이전트 모두 동일한 로컬 검사

**파이프라인:**

```
pre-commit (병렬):
  ├── spotless — ./gradlew spotlessCheck (*.java 변경 시)
  ├── checkstyle — ./gradlew checkstyleMain (*.java 변경 시)
  └── compile — ./gradlew compileJava (*.java 변경 시)

pre-push:
  └── rewrite — ./gradlew rewriteDryRun
```

**설정 파일:** `lefthook.yml`

### 2. GitHub Actions — 원격 CI (4개 잡)

**역할:** PR/push 시 전체 검증

| 잡 | 범위 | 설명 |
|----|------|------|
| `code-quality` | 전체 모듈 | Spotless, Checkstyle, OpenRewrite dry-run |
| `unit-and-slice` | domain, application, autoconfiguration, adapter-input-*, adapter-output-cache | ArchUnit, 단위/슬라이스 테스트 |
| `integration-test` | adapter-output-persist, boot-api | Testcontainers 기반 DB/외부 서비스 |
| `coverage-report` | 루트 | JaCoCo 집계 리포트 |

**설정 파일:** `.github/workflows/test.yml`

**Gradle 캐시:** `gradle/actions/setup-gradle@v4` 사용. main 브랜치가 아닌 경우 read-only.

### 3. JaCoCo — 코드 커버리지

**역할:** 테스트 커버리지 측정 및 집계

**선택 이유:**
* Gradle에 내장된 플러그인으로 별도 설치 불필요
* `jacoco-report-aggregation`으로 멀티 모듈 집계 지원
* GitHub Actions에서 아티팩트로 업로드하여 PR에서 확인 가능

**설정:** `build.gradle.kts`의 `java` 라벨 블록에서 자동 적용

### 4. OpenRewrite (7.29.0) — 코드 현대화

**역할:** 코드 마이그레이션 및 현대화 자동 제안

**선택 이유:**
* Java 25 마이그레이션, Spring Boot 4 업그레이드 레시피 제공
* `rewriteDryRun`으로 안전하게 변경 사항 미리 확인
* 정적 분석 개선 레시피(`CommonStaticAnalysis`)

**커스텀 레시피 (`rewrite.yml`):**
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: io.github.ppzxc.template.CodeQuality
recipeList:
  - org.openrewrite.staticanalysis.CommonStaticAnalysis
  - org.openrewrite.staticanalysis.JavaApiBestPractices
  - org.openrewrite.java.migrate.UpgradeToJava25
  - org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
```

**실행:** `./gradlew rewriteRun` (적용), `./gradlew rewriteDryRun` (미리보기)

## 전체 파이프라인 흐름

```
로컬 개발
  └── pre-commit: spotless + checkstyle + compile (병렬, 빠름)
  └── pre-push: rewriteDryRun (현대화 제안)

원격 CI (PR/push to main)
  ├── code-quality: spotless + checkstyle + rewriteDryRun
  ├── unit-and-slice: ArchUnit + 단위 테스트
  ├── integration-test: Testcontainers 기반 통합 테스트
  └── coverage-report: JaCoCo 집계 (unit + integration 의존)
```

## 검토한 대안

| 대안 | 미채택 이유 |
|------|-----------|
| Husky | Node.js 의존, Java 프로젝트에 불필요한 의존성 추가 |
| pre-commit (Python) | Python 환경 필요, Lefthook이 더 단순 |
| Jenkins | 별도 서버 필요, GitHub Actions로 충분 |
| SonarCloud | 유료 플랜 필요, 현재 규모에서 과도 |
| Codecov | 별도 토큰/설정 필요, JaCoCo 아티팩트로 충분 |

## 관련 문서

→ [ci-tools.md](../.claude/rules/ci-tools.md) — 도구 실행 명령 참조
→ [ADR-0005](0005-code-quality-toolchain.md) — 코드 품질 도구 선택 근거
