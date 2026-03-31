---
status: accepted
date: 2026-03-31
updated: 2026-04-01
decision-makers: ppzxc
---

# 모듈 레이아웃: template/ 하위 플랫 구조 채택

## 배경 및 문제

Hexagonal Architecture 기반 Gradle 멀티 모듈 프로젝트의 디렉토리 구조를 결정해야 한다.
초기에는 `template/apps/`, `template/modules/`, `template/libs/` 세 개의 중간 그룹 폴더를 사용했으나,
모듈 이름 자체(`template-adapter-*`, `template-boot-*`)에 이미 역할이 인코딩되어 있어
중간 폴더가 중복 정보를 만들어냈다.

또한 프로젝트 초기 12개로 시작한 모듈을 재검토한 결과, 범용 보일러플레이트에 불필요한 모듈이 포함되어 있었다.

## 결정 기준

* 루트 디렉토리는 빌드/설정 파일만 위치 (모듈 폴더 혼재 금지)
* 모듈 이름이 자체적으로 역할을 설명할 것
* 불필요한 중첩 제거
* 범용 보일러플레이트에 적합한 최소 모듈 구성

## 검토한 옵션

* **A. template/ 하위 플랫 구조 (채택)** — `template/` 래퍼 유지, 중간 그룹 폴더 제거
* **B. 루트 플랫 구조** — 모든 모듈을 프로젝트 루트에 직접 배치
* **C. Hexagonal 레이어별 그룹** — `core/`, `adapters/`, `bootstrap/`로 그룹화
* **D. 역할별 그룹 (이전 구조)** — `apps/`, `modules/`, `libs/`로 그룹화

## 결정

**A. template/ 하위 플랫 구조** 채택.

루트 디렉토리를 빌드/설정 파일 전용으로 유지하면서, 모든 모듈을 `template/` 하위에 플랫하게 배치한다.
중간 그룹 폴더를 제거하여 탐색 깊이를 한 단계 줄인다.

### 최종 모듈 구성 (8개)

초기 12개에서 재설계를 거쳐 8개로 정리:

| 모듈 | 레이어 | 역할 |
|------|--------|------|
| `template-domain` | Domain | 순수 도메인 모델 (Spring/JPA 금지) |
| `template-application` | Application | Inbound/Outbound Port + UseCase 구현체 (Spring 금지) |
| `template-application-autoconfiguration` | Application | UseCase를 Spring Bean으로 등록 |
| `template-adapter-input-api` | Inbound Adapter | REST Controller + Spring Security |
| `template-adapter-input-ws` | Inbound Adapter | WebSocket Handler |
| `template-adapter-output-persist` | Outbound Adapter | JPA/Flyway |
| `template-adapter-output-cache` | Outbound Adapter | Cache |
| `template-boot-api` | Boot | Spring Boot 애플리케이션 진입점 |

### 제거된 모듈 및 이유

| 제거 모듈 | 이유 |
|-----------|------|
| `template-common` | Domain이 Common에 의존하는 anti-pattern. Domain은 자기완결적이어야 함. 공통 타입은 Domain 내부에 배치. |
| `template-adapter-output-channel` | 범용 보일러플레이트에 SMS/KakaoTalk 특화 모듈 불필요. 필요 시 추가 가능. |
| `template-adapter-output-notify` | 범용 보일러플레이트에 Email/Push 특화 모듈 불필요. 필요 시 추가 가능. |
| `template-boot-admin` | API 서버 하나로 충분. Admin 기능은 보안 설정으로 분리 가능. |

### 리네임

* `template-adapter-input-web` → `template-adapter-input-api`: "web"은 HTTP만 연상시키지만 "api"가 REST API Adapter 역할을 더 명확하게 표현.

### 결과

* 중간 폴더(apps/modules/libs) 제거로 탐색 깊이 감소
* 루트 디렉토리가 빌드/설정 파일만 포함
* 새 모듈 추가 시 `template/` 하위 디렉토리 생성 + `settings.gradle.kts` 한 줄 추가로 완료
* `module()` 헬퍼는 `template/` 래퍼 때문에 필요 (projectDir 매핑)

## 옵션 비교

### B. 루트 플랫 구조
* 장점: `settings.gradle.kts`에서 `module()` 헬퍼 불필요
* 단점: 8개 모듈 폴더와 빌드 파일이 루트에 혼재하여 혼란

### C. Hexagonal 레이어별 그룹
* 장점: 폴더 구조에서 아키텍처 의도가 명확
* 단점: 모듈 이름과 폴더 이름이 중복 정보를 생성
* 단점: 중첩 깊이가 이전 구조와 유사

### D. 역할별 그룹 (이전 구조)
* 장점: 앱/코어/라이브러리가 시각적으로 구분
* 단점: apps/modules/libs 그룹이 모듈 이름 접두사와 중복
* 단점: 세 단계 중첩으로 탐색 속도 저하

## 추가 정보

**module() 헬퍼가 필요한 이유:**
모듈이 `template/` 하위에 있으면 Gradle의 표준 `include()`는 프로젝트 루트에서 모듈을 찾는다.
`module()` 헬퍼가 `projectDir`을 설정하여 Gradle 모듈명을 `template/` 하위의 실제 경로에 매핑한다.
