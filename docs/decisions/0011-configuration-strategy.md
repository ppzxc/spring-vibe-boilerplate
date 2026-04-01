---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 환경 설정 전략: application-{profile}.yml 분리 + 환경변수 오버라이드 패턴

## 배경 및 문제

Spring Boot 앱은 local, test, prod 세 환경에서 다른 설정이 필요하다.
설정 파일에 비밀(DB 패스워드, API 키 등)을 평문으로 저장하면 보안 위협이 된다.
Spring Cloud Config 같은 별도 서버는 소규모 프로젝트에 과도한 복잡도를 추가한다.
12-Factor App 원칙에 따라 설정과 코드를 분리하되, 운영 단순성을 유지해야 한다.

## 결정 기준

* 환경별 설정 분리 (local, test, prod)
* 비밀 정보를 코드 저장소에 커밋하지 않음
* 외부 설정 서버 없이 자체 완결
* 12-Factor App 원칙 준수

## 결정

**application-{profile}.yml 분리 + 환경변수 오버라이드 패턴** 을 채택한다.

### 설정 파일 구조

파일 위치: `template-boot-api/src/main/resources/`

| 파일 | 용도 |
|------|------|
| `application.yml` | 공통 설정 + 환경변수 플레이스홀더 |
| `application-local.yml` | 로컬 개발 설정 (개발자 기본값) |
| `application-test.yml` | 테스트 설정 (Testcontainers 연동) |
| `application-prod.yml` | 프로덕션 — 환경변수 주입 중심, 비밀 없음 |

핵심 제약:

1. **환경변수 플레이스홀더**: `application.yml`에 기본값 포함 형식 사용
   ```yaml
   spring.datasource.url: ${DB_URL:jdbc:postgresql://localhost:5432/template}
   ```
2. **비밀 정보 금지**: DB 패스워드 등 비밀은 반드시 환경변수로 주입, yml 평문 저장 금지
3. **prod 설정**: `application-prod.yml`은 환경변수 레퍼런스만 포함, 실제 값 없음
4. **test 설정**: `application-test.yml`은 Testcontainers 동적 포트 연동 패턴 사용
5. **Spring Cloud Config 미채택**: 별도 서버 필요, 소규모 프로젝트에 과도
6. **Vault 미채택**: 운영 복잡도 증가 — 선택적 확장으로만 언급, 기본 전략에서 제외
7. **프로파일 활성화**: `SPRING_PROFILES_ACTIVE=prod` 환경변수로 컨테이너에서 설정

### 환경변수 목록 (주요)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/template` |
| `DB_USERNAME` | DB 사용자명 | `template` |
| `DB_PASSWORD` | DB 패스워드 | 없음 (필수) |
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `local` |

## 검토한 대안

| 대안 | 미채택 이유 |
|------|-----------|
| 환경변수 전용 (yml 없음) | 설정 가시성 저하, 기본값 관리 어려움 |
| Spring Cloud Config | 별도 Config Server 필요, 소규모 프로젝트 과도한 인프라 |
| Vault (HashiCorp) | 운영 복잡도 증가, 선택적 확장으로 가이드 |
| `.env` 파일 커밋 | 보안 위협 (.gitignore 실수 위험) |

## 관련 문서

→ [ADR-0010](0010-containerization-strategy.md) — docker-compose 환경변수 주입 패턴
→ [ADR-0008](0008-observability-strategy.md) — OTel 엔드포인트 환경변수 패턴
