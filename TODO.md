# TODO — Template Boilerplate Roadmap

> 업계 리더(JHipster, Spring Modulith, Netflix OSS)와 비교하여 도출한 보강 항목.
> 각 Phase는 이전 Phase 완료 후 진행을 권장하나, 독립 항목은 병렬 가능.

---

## Phase 1: 기반 인프라 (템플릿을 실제로 쓸 수 있는 최소 요건)

- [x] **ErrorCode enum** — `template-domain`에 AIP-193 기반 공통 에러 코드 정의
- [x] **GlobalExceptionHandler** — `template-adapter-input-api`에서 AIP-193 + ProblemDetail(RFC 9457) 변환
- [ ] **Security 기본 설정** — (ADR-0016 완료, 코드 미포함) 시나리오별 구현 가이드 참조
- [ ] **영속화 기술 선택 및 설정** — (ADR-0018 완료, 코드 미포함) DB + ORM + Migration 선택 가이드 참조
- [ ] **공통 Pagination/Sort DTO** — 페이지네이션 요청/응답 래퍼
- [ ] **샘플 도메인 1개** — Domain → Port → UseCase → Adapter 전 레이어 관통 예제 (CRUD)
- [ ] **레이어별 샘플 테스트** — 단위(Service), 슬라이스(@WebMvcTest), 통합(Testcontainers) 각 1개 이상
- [ ] **docker-compose 인프라 서비스** — DB 서비스 추가 + health check (영속화 선택 후)

---

## Phase 2: 운영 성숙도 (프로덕션 운영 준비)

- [ ] **Resilience4j 통합** — Circuit Breaker, Retry, Timeout, Bulkhead 설정 + 어노테이션 방식
- [ ] **Rate Limiting** — Bucket4j 또는 Spring Cloud Gateway 기반 요청 제한
- [ ] **Custom HealthIndicator** — DB 연결, 외부 서비스 상태 체크
- [ ] **Custom Metrics** — Micrometer 커스텀 메트릭 (비즈니스 KPI 계측)
- [ ] **Request/Response 로깅** — HTTP 요청/응답 구조화 로깅 (민감 정보 마스킹)
- [ ] **MDC RequestId 필터** — (ADR-0008 구현 가이드 완료, 코드 미포함) 요청별 고유 ID MDC 주입 필터
- [ ] **Cache 구현** — (ADR-0019 완료, 코드 미포함) Caffeine 기본 + Redis 확장 경로 참조
- [ ] **API Versioning 전략** — URL/Header 기반 버전 관리 패턴 적용

---

## Phase 3: DevOps / 자동화 (CI/CD 완성)

- [x] **Dependabot** — 의존성 자동 업데이트 PR (`.github/dependabot.yml`)
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
- [ ] **Multi-tenancy 구현** — Row-level + PostgreSQL RLS 코드 구현 (ADR-0015 완료, `rules/multi-tenancy.md` 완료)

---

## 참고: 현재 완료된 항목

- [x] Hexagonal Architecture 8모듈 구조
- [x] ArchUnit 아키텍처 테스트 (Domain, Application)
- [x] 코드 품질 도구 7종 (Spotless, Checkstyle, ErrorProne, NullAway, OpenRewrite, ArchUnit, JaCoCo)
- [x] CI 파이프라인 4단계 (quality → unit → integration → coverage)
- [x] Virtual Thread 안전성 강제 (synchronized/ThreadLocal 금지)
- [x] ADR 19개 + 규칙 파일 13개
- [x] 구조화 로깅 설정 (Spring Boot 4 native)
- [x] Actuator 엔드포인트 설정
- [x] OTel 의존성 + OTLP 환경변수 설정
- [x] Dockerfile (멀티스테이지 빌드)
- [x] Lefthook Git Hooks (pre-commit, pre-push)
- [x] MapStruct + Lombok 빌드 설정
- [x] NullMarked (JSpecify) 전 모듈 적용
- [x] AIP-193 + ProblemDetail 에러 모델 (ADR-0017)
- [x] MDC RequestId 구현 가이드 (ADR-0008 보충)
- [x] Graceful Shutdown 설정
- [x] 환경변수 문서화 (`.env.example`)
- [x] Dependabot 의존성 자동 업데이트
