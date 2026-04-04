---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 모듈 레이아웃: template/ 하위 플랫 구조

## Context and Problem Statement

Hexagonal Architecture 기반 Gradle 멀티모듈 프로젝트에서 8개 모듈을 어떤 디렉토리 구조로 배치할지 결정해야 한다.
모듈 이름 접두사(`template-adapter-*`, `template-boot-*`)가 이미 레이어 역할을 인코딩하고 있으므로,
디렉토리 구조는 이 정보를 중복하지 않으면서 탐색이 간결해야 한다.

## Decision Drivers

- 루트 디렉토리는 빌드/설정 파일만 배치 (모듈 폴더 혼재 금지)
- 모듈 이름이 역할을 자체 설명할 것
- 불필요한 중첩 제거
- 새 모듈 추가가 단순할 것

## Considered Options

- **A. template/ 하위 플랫 구조 (채택)** — `template/` 래퍼 안에 모든 모듈을 나란히 배치
- **B. 루트 플랫 구조** — 모든 모듈을 프로젝트 루트에 직접 배치
- **C. Hexagonal 레이어별 그룹** — `core/`, `adapters/`, `bootstrap/`로 그룹화

## Decision Outcome

**A. template/ 하위 플랫 구조** 채택.

루트 디렉토리를 빌드/설정 파일 전용으로 유지하면서, 모든 모듈을 `template/` 하위에 플랫하게 배치한다.

### 모듈 구성 (8개)

| 모듈 | 레이어 | 역할 |
|------|--------|------|
| `template-domain` | Domain | 순수 도메인 모델 (Spring/JPA 금지) |
| `template-application` | Application | Inbound/Outbound Port + UseCase 구현체 (Spring 금지) |
| `template-application-autoconfiguration` | Application | UseCase를 Spring Bean으로 등록 |
| `template-adapter-input-api` | Inbound Adapter | REST Controller + Spring Security |
| `template-adapter-input-ws` | Inbound Adapter | WebSocket Handler |
| `template-adapter-output-persist` | Outbound Adapter | JPA + Flyway |
| `template-adapter-output-cache` | Outbound Adapter | Cache |
| `template-boot-api` | Boot | Spring Boot 애플리케이션 진입점 (port 8080) |

```
template/
  template-domain/
  template-application/
  template-application-autoconfiguration/
  template-adapter-input-api/
  template-adapter-input-ws/
  template-adapter-output-persist/
  template-adapter-output-cache/
  template-boot-api/
```

### 구조의 이점

- 탐색 깊이 1단계 (`template/<module-name>/`)
- 루트 디렉토리 = 빌드/설정 파일 전용
- 새 모듈 추가 = `template/` 하위 디렉토리 생성 + `settings.gradle.kts` 한 줄 추가
- `module()` 헬퍼가 `projectDir`을 매핑하여 Gradle 모듈명과 실제 경로를 연결

### 새 모듈 추가 방법

상세 절차는 `rules/module-add.md` 참조.

## Pros and Cons of the Options

### B. 루트 플랫 구조

- 장점: `settings.gradle.kts`에서 `module()` 헬퍼 불필요
- 단점: 8개 모듈 폴더와 빌드 파일이 루트에 혼재하여 탐색 혼란

### C. Hexagonal 레이어별 그룹

- 장점: 폴더 구조에서 아키텍처 의도가 시각적으로 명확
- 단점: 모듈 이름과 폴더 이름이 중복 정보를 생성 (`adapters/template-adapter-*`)
- 단점: 중첩 깊이가 A 대비 1단계 증가

## More Information

**module() 헬퍼가 필요한 이유:**
모듈이 `template/` 하위에 있으면 Gradle의 표준 `include()`는 프로젝트 루트에서 모듈을 찾는다.
`module()` 헬퍼가 `projectDir`을 설정하여 Gradle 모듈명(`:template-domain`)을 실제 경로(`template/template-domain/`)에 매핑한다.
