---
description: CI/CD 핵심 정책 — 보안 스캔 게이트, 배포 전략, 환경별 설정
alwaysApply: true
---

# CI/CD 정책 Rules

CI/CD 핵심 정책 — 항상 로드.

> **요구 수준 키워드**: MUST, MUST NOT, SHOULD는 RFC 2119 기준.
> 인프라 설정(Jenkinsfile, GitHub Actions workflow 상세)은 이 파일 범위 외.

---

## 1. CI 필수 게이트

모든 PR은 아래 게이트를 통과해야 병합된다.

| 게이트 | 도구 | 실패 시 |
|--------|------|--------|
| 컴파일 | Gradle `compileJava` | 즉시 차단 |
| 단위/통합 테스트 | `./gradlew test` | 즉시 차단 |
| 커버리지 (domain, app) | JaCoCo 80% LINE | 즉시 차단 |
| 코드 품질 | Spotless + Checkstyle | 즉시 차단 |
| 정적 분석 | ErrorProne + NullAway | 즉시 차단 |
| Modulith 구조 검증 | `ApplicationModules.verify()` | 즉시 차단 |
| **보안 취약점 스캔** | OWASP Dependency-Check | CVSS 7.0+ 즉시 차단 |

- MUST: CVSS 7.0 이상 취약점이 있는 의존성은 PR 병합 전에 업데이트하거나 억제(`suppress`) ADR을 작성한다.
- MUST NOT: 보안 스캔을 skip하거나 threshold를 임의로 높인다.

---

## 2. 보안 스캔 설정

```kotlin
// boilerplate-boot-api/build.gradle.kts
plugins {
    id("org.owasp.dependencycheck") version "latest"
}

dependencyCheck {
    failBuildOnCVSS = 7.0f          // CVSS 7.0+ = 빌드 실패
    suppressionFile = "dependency-check-suppressions.xml"
}
```

```xml
<!-- dependency-check-suppressions.xml 억제 형식 -->
<suppressions>
  <suppress>
    <notes>ADR-XXXX: [근거] 업그레이드 불가 이유</notes>
    <cve>CVE-XXXX-XXXXX</cve>
  </suppress>
</suppressions>
```

- MUST: 억제된 CVE는 반드시 ADR 번호와 근거를 포함한다.
- MUST: 억제된 CVE는 분기별 재검토한다.

---

## 3. 배포 전략 원칙

| 환경 | 전략 | 설명 |
|------|------|------|
| **운영(prod)** | Blue-Green 또는 Canary | 무중단 배포 필수 |
| **스테이징(staging)** | Rolling Update | 이전 버전과 새 버전 동시 실행 가능해야 함 |
| **개발(dev)** | 직접 교체 | 가용성 SLA 없음 |

- MUST: 운영 배포는 Blue-Green 또는 Canary 중 하나를 선택한다.
- MUST: DDL 변경은 Backward Compatible이어야 한다 (Rolling Update 중 구/신 앱 동시 실행 — scaffold.md §DDL Backward Compatibility 참조).
- MUST NOT: 운영에 직접 교체(In-place)로 배포한다.

---

## 4. 환경별 설정 분리

- MUST: `application.yml` 기본값 + `application-{env}.yml` 환경별 오버라이드 패턴 사용.
- MUST: 시크릿(DB 비밀번호, JWT Secret, API Key)은 환경 변수로 주입한다. 파일에 하드코딩 금지 (security.md §10 참조).
- MUST: `spring.profiles.active`는 배포 파이프라인에서 주입한다. 코드에 하드코딩 금지.

```yaml
# application.yml — 기본값 (하드코딩 금지)
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

---

## 5. 헬스체크 / 배포 완료 기준

- MUST: `/actuator/health/readiness` 가 `UP`을 반환한 후 트래픽 라우팅을 전환한다.
- MUST NOT: 컨테이너 시작 직후 바로 트래픽을 라우팅한다 (Spring Context 초기화 시간 필요).
- MUST: Liveness probe 실패 시 컨테이너를 재시작하는 정책을 설정한다.

---

## fallback 지시문

> 위 규칙을 현재 상황에 적용하기 어렵거나 규칙 간 충돌이 발생하면,
> 관련 ADR을 `docs/decisions/`에서 직접 읽어 결정 배경을 파악하라.
