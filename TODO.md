# TODO — Template Boilerplate Roadmap

> 업계 리더(JHipster, Spring Modulith, Netflix OSS)와 비교하여 도출한 보강 항목.
> 각 Phase는 이전 Phase 완료 후 진행을 권장하나, 독립 항목은 병렬 가능.

---

## Phase 1: 기반 인프라 (템플릿을 실제로 쓸 수 있는 최소 요건)

- [ ] **Security 기본 설정** — SecurityFilterChain, CORS, CSRF 정책, 인증 방식 선택(JWT / OAuth2)
- [ ] **ErrorCode enum** — `template-domain`에 공통 에러 코드 정의
- [ ] **GlobalExceptionHandler** — `template-adapter-input-api`에서 ProblemDetail(RFC 9457) 변환
- [ ] **영속화 기술 선택 및 설정** — DB 선택(PostgreSQL 등), Spring Data JPA / R2DBC 의존성, HikariCP 설정
- [ ] **DB Migration 도구** — Flyway 또는 Liquibase 설정 + 초기 마이그레이션 스크립트
- [ ] **공통 Pagination/Sort DTO** — 페이지네이션 요청/응답 래퍼
- [ ] **샘플 도메인 1개** — Domain → Port → UseCase → Adapter 전 레이어 관통 예제 (CRUD)
- [ ] **레이어별 샘플 테스트** — 단위(Service), 슬라이스(@WebMvcTest), 통합(Testcontainers) 각 1개 이상
- [ ] **docker-compose 인프라 서비스** — DB, (선택) Redis, (선택) MQ 서비스 추가 + health check

---

## Phase 2: 운영 성숙도 (프로덕션 운영 준비)

- [ ] **Resilience4j 통합** — Circuit Breaker, Retry, Timeout, Bulkhead 설정 + 어노테이션 방식
- [ ] **Rate Limiting** — Bucket4j 또는 Spring Cloud Gateway 기반 요청 제한
- [ ] **Custom HealthIndicator** — DB 연결, 외부 서비스 상태 체크
- [ ] **Custom Metrics** — Micrometer 커스텀 메트릭 (비즈니스 KPI 계측)
- [ ] **Request/Response 로깅** — HTTP 요청/응답 구조화 로깅 (민감 정보 마스킹)
- [ ] **MDC RequestId 필터** — 요청별 고유 ID MDC 주입 필터
- [ ] **Graceful Shutdown** — Spring Boot graceful shutdown + 커넥션 드레인
- [ ] **Cache 구현** — `template-adapter-output-cache`에 Redis/Caffeine 기반 캐시 어댑터
- [ ] **API Versioning 전략** — URL/Header 기반 버전 관리 패턴 적용

---

## Phase 3: DevOps / 자동화 (CI/CD 완성)

- [ ] **Dependabot 또는 Renovate** — 의존성 자동 업데이트 PR
- [ ] **CD 파이프라인** — GitHub Actions 배포 워크플로 (staging → production)
- [ ] **Docker 이미지 빌드/푸시** — CI에서 이미지 빌드 + Container Registry 푸시
- [ ] **Release 워크플로** — semantic-release 또는 수동 태그 기반 릴리스
- [ ] **Environment별 docker-compose** — local / staging / production 분리
- [ ] **Kubernetes 매니페스트** — (선택) Deployment, Service, ConfigMap, Secret 템플릿
- [ ] **Helm Chart** — (선택) K8s 패키징

---

## Phase 4: 고급 패턴 (도메인 복잡도 대응)

- [ ] **Domain Event 발행** — ApplicationEventPublisher 기반 도메인 이벤트
- [ ] **비동기 처리** — `@Async` 또는 Message Queue 기반 비동기 커맨드 처리
- [ ] **Message Queue 통합** — (선택) Kafka / RabbitMQ adapter 모듈
- [ ] **Idempotency Key** — 멱등키 기반 중복 요청 방어
- [ ] **Distributed Lock** — `LockPort` 구현 (Redis / DB 기반)
- [ ] **CQRS Read Model** — 읽기 전용 모델 분리 (선택적 적용)
- [ ] **Feature Flag** — (선택) 피처 플래그 시스템 연동
- [ ] **Multi-tenancy** — Row-level + PostgreSQL RLS 구현 (ADR-0015, `rules/multi-tenancy.md`)

---

## 참고: 현재 완료된 항목

- [x] Hexagonal Architecture 8모듈 구조
- [x] ArchUnit 아키텍처 테스트 (Domain, Application)
- [x] 코드 품질 도구 7종 (Spotless, Checkstyle, ErrorProne, NullAway, OpenRewrite, ArchUnit, JaCoCo)
- [x] CI 파이프라인 4단계 (quality → unit → integration → coverage)
- [x] Virtual Thread 안전성 강제 (synchronized/ThreadLocal 금지)
- [x] ADR 14개 + 규칙 파일 12개
- [x] 구조화 로깅 설정 (Spring Boot 4 native)
- [x] Actuator 엔드포인트 설정
- [x] OTel 의존성 + OTLP 환경변수 설정
- [x] Dockerfile (멀티스테이지 빌드)
- [x] Lefthook Git Hooks (pre-commit, pre-push)
- [x] MapStruct + Lombok 빌드 설정
- [x] NullMarked (JSpecify) 전 모듈 적용
