---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 코드 품질 도구 전략: Spotless + Checkstyle + ErrorProne + NullAway

## Context and Problem Statement

Java 25 기반 Spring Boot 4 프로젝트에서 일관된 코드 스타일과 정적 분석을 자동으로 강제할 도구를 선택해야 한다.
팀원이나 AI 에이전트가 코드를 작성할 때도 동일한 품질 기준이 유지되어야 한다.

## Decision Drivers

* 자동 수정 가능한 포맷팅 도구
* 네이밍 규칙 및 금지 패턴 강제
* 컴파일 타임 버그 탐지
* Null 안전성 강제 (Virtual Thread 환경에서 NPE는 디버깅이 어려움)
* Virtual Thread 안전성 강제 (`synchronized`, `ThreadLocal` 금지)

## Decision Outcome

Chosen option: "Spotless + Checkstyle + ErrorProne + NullAway", because 각 도구가 자동 포맷, 네이밍/금지패턴, 버그 탐지, Null 안전성 역할을 분담하여 중복 없이 품질을 보장.

### 1. Spotless (Google Java Format 1.35.0)

**역할:** 코드 포맷 자동 수정

**선택 이유:**
* Google Java Format은 토론 없이 수용 가능한 업계 표준 포맷
* `spotlessApply`로 자동 수정. 개발자가 포맷을 신경 쓸 필요 없음
* Checkstyle과 역할 분담: Spotless = 자동수정, Checkstyle = 수동 규칙

**설정:** `build.gradle.kts`의 `spotless { java { googleJavaFormat("1.35.0") } }`

**실행:** `./gradlew spotlessApply` (수정), `./gradlew spotlessCheck` (검사)

### 2. Checkstyle (10.21.4)

**역할:** 네이밍 규칙, 구조 규칙, Virtual Thread 안전성 강제

**선택 이유:**
* Spotless가 자동 수정할 수 없는 규칙(네이밍, 금지 패턴) 담당
* Virtual Thread 안전성: `synchronized` → `ReentrantLock` 강제, `ThreadLocal` → `ScopedValue` 강제
* 허용 약어 목록으로 업계 표준 약어(API, URL, DTO, JWT 등) 허용

**주요 규칙:**
* `synchronized` 키워드 금지 (Virtual Thread 핀닝 방지)
* `ThreadLocal` 금지 (Virtual Thread 누수 방지)
* 줄 길이 120자 제한
* 탭 문자 금지
* 와일드카드 import 금지

**설정:** `config/checkstyle/checkstyle.xml`

**실행:** `./gradlew checkstyleMain`

### 3. ErrorProne (2.36.0)

**역할:** 컴파일 타임 버그 패턴 탐지

**선택 이유:**
* 컴파일 시점에 알려진 버그 패턴을 탐지하여 런타임 오류 예방
* NullAway의 호스트 역할 (NullAway는 ErrorProne 플러그인)
* Google이 내부적으로 사용하는 검증된 도구

**설정:** `build.gradle.kts`의 `errorprone { ... }` 블록, 컴파일 시 자동 실행

### 4. NullAway (0.12.3) + JSpecify (1.0.0)

**역할:** Null 안전성 컴파일 타임 강제

**선택 이유:**
* `@Nullable` / `@NonNull` 어노테이션을 기반으로 NPE 가능성을 컴파일 타임에 탐지
* JSpecify는 표준화된 null 어노테이션 스펙 (`org.jspecify.annotations`)
* Virtual Thread 환경에서 NPE는 스택 추적이 복잡 — 컴파일 타임 방지가 훨씬 효과적

**사용법:** `@Nullable`, `@NonNull` 어노테이션을 메서드 시그니처에 명시

**설정:** ErrorProne 플러그인으로 동작, 컴파일 시 자동 실행

## 도구 간 역할 분담

| 도구 | 시점 | 역할 | 자동수정 |
|------|------|------|----------|
| Spotless | pre-commit / CI | 코드 포맷 | ✓ (`spotlessApply`) |
| Checkstyle | pre-commit / CI | 네이밍, 구조, 금지패턴 | ✗ (수동 수정) |
| ErrorProne | 컴파일 | 버그 패턴 탐지 | ✗ |
| NullAway | 컴파일 | Null 안전성 | ✗ |

## Pros and Cons of the Options

| 도구 | 미채택 이유 |
|------|-----------|
| PMD | ErrorProne과 역할 중복, ErrorProne이 Java 최신 기능 지원 우수 |
| SpotBugs | 바이트코드 분석 (런타임 필요), ErrorProne의 컴파일타임 분석이 선호됨 |
| SonarQube | 별도 서버 필요, CI 복잡도 증가 |
| Palantir Java Format | Google Java Format 대비 커뮤니티 규모 작음 |
| `@javax.annotation.Nullable` | JSpecify가 최신 표준, 더 정확한 null 분석 지원 |

## More Information

→ [ci-tools.md](../rules/ci-tools.md) — 도구 실행 명령 및 Git hook 설정
