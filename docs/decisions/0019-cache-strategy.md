---
status: accepted
date: 2026-04-03
decision-makers: ppzxc
---

# 캐시 전략: Caffeine 기본 + Redis 확장 경로

## Context and Problem Statement

`boilerplate-adapter-output-cache` 모듈에 `spring-boot-starter-cache`만 존재하고
구체적인 캐시 구현체가 미정이다.
업계 표준에 기반한 기본 선택지를 제공하되, 분산 캐시로의 확장 경로도 안내해야 한다.

## Decision Drivers

* 외부 인프라 의존성 최소화 (Redis 서버 등 불필요)
* JVM 생태계에서 검증된 표준 구현체
* Spring Boot 자동 구성 호환
* 분산 캐시(Redis)로의 자연스러운 확장 가능성

## Considered Options

1. Caffeine (로컬 캐시)
2. Redis (분산 캐시)
3. EhCache 3 (JCache 경유)
4. ConcurrentMapCache (Spring 기본 fallback)

## Decision Outcome

Chosen option: "Caffeine", because JVM 로컬 캐시 사실상 표준이며,
외부 인프라 없이 Spring Boot 자동 구성으로 동작한다.
Redis로의 확장은 별도 경로로 안내한다.

### Consequences

* Good, because 외부 인프라(Redis 서버) 없이 즉시 사용 가능
* Good, because Google Guava Cache 후속으로 업계에서 가장 널리 검증됨
* Good, because classpath 추가만으로 Spring Boot CacheAutoConfiguration 자동 구성
* Bad, because 멀티 인스턴스 환경에서 캐시 일관성 미보장 (로컬 캐시 한계)

## Caffeine 선택 근거

| 근거 | 설명 |
|------|------|
| JVM 생태계 표준 | Google Guava Cache 후속 (같은 저자 Ben Manes). Spring Framework 5.0에서 Guava Cache 제거, Caffeine을 공식 대체제로 명시 |
| 최고 적중률 | Window TinyLFU 알고리즘으로 LRU, LFU, ARC 등 전통 알고리즘 대비 최고 캐시 적중률 (학술 논문 기반) |
| 핵심 프로젝트 내부 사용 | Apache Kafka, Cassandra, Neo4j, Hibernate ORM이 내부 캐시로 사용 |
| Spring 기본 통합 | Spring Boot `CacheAutoConfiguration` 우선순위에서 JCache 다음으로 높음 |
| GitHub stars 15k+ | 활발한 유지보수 (2025-2026 지속 릴리스) |

## 확장 경로: L1/L2 캐시 패턴

업계 표준인 "Near Cache" 또는 "L1/L2 캐시" 패턴으로 분산 캐시를 추가할 수 있다.

```
[요청] → [L1: Caffeine ~100ns] → miss → [L2: Redis ~1ms] → miss → [DB ~10ms]
```

| 계층 | 역할 | 지연시간 | 용량 |
|------|------|---------|------|
| L1 Caffeine | 핫 데이터 로컬 캐시 | ~100ns | JVM 힙 의존 |
| L2 Redis | 인스턴스 간 공유 캐시 | ~0.5-2ms | 수십 GB~TB |

### 업계 사례

* **Netflix**: EVCache(Memcached 기반) + 로컬 캐시 2-tier 구조
* **Uber**: CacheFront (로컬+분산 2-tier, Redis 호출 40% 이상 감소 보고)
* **대규모 기업 공통**: 모든 대규모 기업이 로컬 + 분산 캐시 조합 사용

### Redis 추가 시 구현 방향

* `boilerplate-adapter-output-cache` 모듈에 `spring-boot-starter-data-redis` 추가
* `CompositeCacheManager` 또는 커스텀 `CacheManager`로 L1(Caffeine)/L2(Redis) 조합
* 캐시 무효화(invalidation) 전략은 프로젝트 요구사항에 따라 선택

## Pros and Cons of the Options

| 대안 | 미채택 이유 |
|------|-----------|
| Redis | 외부 인프라 의존성 추가, 단일 인스턴스 서비스에 과도 — 확장 경로로 별도 안내 |
| EhCache 3 | JSR-107(JCache) 경유 필요, XML 설정 복잡, Caffeine 대비 성능/편의 이점 없음 |
| ConcurrentMapCache | TTL/크기 제한 없음, 메모리 누수 위험, 프로덕션 부적합 (Spring Boot의 fallback 전용) |

## More Information

→ [ADR-0002](0002-flat-module-structure.md) — 모듈 레이아웃 (boilerplate-adapter-output-cache)
→ [architecture.md](../../.claude/rules/architecture.md) — 레이어 의존성 규칙
