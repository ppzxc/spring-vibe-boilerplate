---
status: accepted
date: 2026-04-05
decision-makers: ppzxc
---

# Virtual Threads 환경에서 HikariCP Pool Size를 DB 연결 수 기준으로 결정한다

## Context and Problem Statement

Java 25 Virtual Threads를 활성화하면 blocking I/O 시 carrier thread가 반환되므로
수천 개의 Virtual Thread가 동시에 실행될 수 있다. 이때 HikariCP pool size를
너무 크게 설정하면 DB max_connections를 초과하고, 너무 작게 설정하면 connection
timeout이 발생한다. 올바른 pool size 결정 기준이 필요하다.

## Decision Outcome

Chosen option: "DB max_connections 기반 상한 설정 + 환경변수 주입", because
Virtual Threads 환경에서 연결 경합의 진짜 병목은 thread 수가 아닌 DB 연결 수이므로
DB 서버의 max_connections × 0.8을 시작값으로 삼고 부하 테스트로 조정한다.

## 구현 사항

- `application.yml`의 `spring.datasource.hikari.*` 설정에 권장 값과 주석 추가
- 모든 pool 파라미터를 환경변수로 주입 가능하게 구성 (`HIKARI_MAX_POOL_SIZE` 등)
- 가이드 문서: `docs/guides/virtual-threads-hikari.md`
- 메트릭 모니터링: `hikaricp.connections.active`, `hikaricp.connections.pending`

## 참고

- [HikariCP — About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [JEP 444 — Virtual Threads](https://openjdk.org/jeps/444)
