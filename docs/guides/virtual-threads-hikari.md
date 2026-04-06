# Virtual Threads + HikariCP Connection Pool 가이드

> 관련 ADR: [ADR-0025](../decisions/0025-virtual-threads-hikari-pool-sizing.md)

## 핵심 원칙

Java 25 Virtual Threads 환경에서 HikariCP는 **전통적인 pool sizing 공식이 적용되지 않는다**.

전통 방식 (Platform Threads):
```
pool size ≈ active thread count
```

Virtual Threads 방식:
```
pool size = min(DB max_connections × 0.8, 실측 동시 DB 쿼리 수)
```

Virtual Thread는 blocking I/O(DB 대기) 시 carrier thread를 반환하므로, thread 수와 pool size를
일치시킬 필요가 없다. **DB 연결 수**가 진짜 병목이다.

## 위험 패턴

### 1. Pool Size를 무한정 크게 설정

```yaml
# 위험: DB 서버가 max_connections를 초과하면 연결 거부됨
hikari:
  maximum-pool-size: 1000
```

### 2. Pool Size를 너무 작게 설정

```yaml
# 위험: 고부하 시 connection timeout 발생
hikari:
  maximum-pool-size: 5
```

### 3. connectionTimeout 미설정

HikariCP 기본값은 30초. Virtual Threads로 요청이 폭증하면 모든 요청이 30초씩 대기한다.
빠른 실패를 위해 더 짧게 설정하는 것을 권장한다.

## 권장 설정

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${HIKARI_MAX_POOL_SIZE:20}   # DB max_connections × 0.8 이하
      minimum-idle: ${HIKARI_MIN_IDLE:5}
      connection-timeout: ${HIKARI_CONNECTION_TIMEOUT:30000}  # 30초 (빠른 실패 원하면 5000)
      idle-timeout: ${HIKARI_IDLE_TIMEOUT:600000}     # 10분
      max-lifetime: ${HIKARI_MAX_LIFETIME:1800000}    # 30분
```

## Pool Size 결정 방법

1. **DB 서버 max_connections 확인**
   ```sql
   SHOW max_connections;  -- PostgreSQL
   ```

2. **시작값 계산**
   ```
   maximumPoolSize = max_connections × 0.8 (다른 클라이언트/관리 커넥션 여유)
   ```
   예: max_connections=100 → maximumPoolSize=80

3. **부하 테스트로 검증**
   ```bash
   # scripts/load-test/todo-crud.js 실행
   k6 run scripts/load-test/todo-crud.js
   ```

4. **Micrometer 메트릭으로 모니터링**
   - `hikaricp.connections.active` — 현재 사용 중인 연결 수
   - `hikaricp.connections.pending` — 연결 대기 중인 요청 수
   - `hikaricp.connections.timeout.total` — timeout 발생 횟수

   `pending > 0` 이 지속되면 pool size 부족. `timeout` 발생 시 즉시 증설.

## Virtual Threads에서 주의할 점

### synchronized 블록 사용 금지

HikariCP 내부에 `synchronized`가 있으면 Virtual Thread가 carrier thread를 **핀닝(pinning)**한다.
HikariCP 6.x 이상에서는 이 문제가 해결되었다. 최신 버전을 유지할 것.

```bash
# 핀닝 감지 JVM 옵션 (개발 시 사용)
-Djdk.tracePinnedThreads=full
```

### connection-timeout vs query timeout

- `connection-timeout`: pool에서 연결을 **빌리는** 시간 제한
- DB query timeout: 실제 쿼리 실행 시간 제한 (jOOQ: `settings.queryTimeout`)

두 설정을 독립적으로 관리할 것.

## 환경변수 목록

| 변수 | 설명 | 권장값 |
|------|------|--------|
| `HIKARI_MAX_POOL_SIZE` | 최대 연결 수 | `max_connections × 0.8` |
| `HIKARI_MIN_IDLE` | 최소 유휴 연결 수 | `5` |
| `HIKARI_CONNECTION_TIMEOUT` | 연결 획득 타임아웃 (ms) | `30000` |
| `HIKARI_IDLE_TIMEOUT` | 유휴 연결 유지 시간 (ms) | `600000` |
| `HIKARI_MAX_LIFETIME` | 연결 최대 수명 (ms) | `1800000` |
