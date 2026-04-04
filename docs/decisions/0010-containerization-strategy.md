---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# 컨테이너화 전략: bootBuildImage (기본) + 멀티스테이지 Dockerfile (대안) + docker-compose

## Context and Problem Statement

컨테이너 이미지 빌드 방식이 여러 가지 존재한다. Spring Boot 공식 Cloud Native Buildpacks,
직접 작성한 Dockerfile, Jib 플러그인 등이 있으며 각각 트레이드오프가 다르다.
Virtual Threads를 사용하는 이 프로젝트에서 GraalVM Native Image는 목적이 중복된다.
보일러플레이트로서 기본 방식과 대안 방식을 모두 제공하여 사용자가 선택할 수 있게 해야 한다.

## Decision Drivers

* Spring Boot 4 공식 지원 방식 우선
* 보안 패치 자동 적용 (베이스 이미지 관리 최소화)
* 로컬 개발 환경 구성 지원 (PostgreSQL 18 포함)
* 특정 JDK 버전 고정 등 완전 제어 필요 시 대안 제공

## Decision Outcome

**bootBuildImage (기본) + 멀티스테이지 Dockerfile (대안) + docker-compose** 조합을 채택한다.

핵심 제약:

1. **기본 빌드**: `./gradlew bootBuildImage` 사용 (Spring Boot 공식 Cloud Native Buildpacks)
2. **Paketo Buildpack 장점**: 보안 패치 자동 적용, JDK 업그레이드 자동화, Spring Boot 4 네이티브 통합
3. **대안 Dockerfile**: 멀티스테이지 Dockerfile 제공 — 특정 JDK 버전 고정, 사이드카 패턴, 커스텀 레이어 필요 시 선택
4. **docker-compose**: `app + PostgreSQL 18` 구성으로 로컬 개발 환경 제공
5. **GraalVM Native Image 미채택**: Virtual Threads와 목적 중복, 빌드 시간 10분+, 리플렉션 제한
6. **이미지 레지스트리**: 프로젝트별 선택 — 보일러플레이트에서 고정하지 않음

```bash
# 기본 빌드 (권장)
./gradlew bootBuildImage

# 대안: Dockerfile 빌드
docker build -t boilerplate-app .

# 로컬 개발 환경
docker compose up -d
```

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| Dockerfile만 | 베이스 이미지 보안 패치 수동 관리 필요, bootBuildImage 장점 포기 |
| bootBuildImage만 | JDK 버전 고정 등 완전 제어 필요 시 대안 없음 |
| Jib | 유지보수 빈도 감소 우려, Gradle/Maven 플러그인 호환성 불확실 |
| GraalVM Native Image | Virtual Threads 사용 목적과 중복, 빌드 시간 10분+, 리플렉션 설정 복잡 |

## More Information

→ [ADR-0006](0006-ci-pipeline-strategy.md) — CI 파이프라인 (GitHub Actions 빌드 잡)
→ [ADR-0011](0011-configuration-strategy.md) — 환경 설정 전략 (prod 환경변수 주입)
