# Architecture Rules

## 모듈 레이아웃 [ADR-0002]

8개 모듈, `boilerplate/` 하위 플랫 구조.

| 모듈 | 레이어 | Spring | JPA |
|------|--------|--------|-----|
| `boilerplate-domain` | Domain | ✗ | ✗ |
| `boilerplate-application` | Application | ✗ | ✗ |
| `boilerplate-application-autoconfiguration` | Application | ✓ | ✗ |
| `boilerplate-adapter-input-api` | Inbound Adapter | ✓ | ✗ |
| `boilerplate-adapter-input-ws` | Inbound Adapter | ✓ | ✗ |
| `boilerplate-adapter-output-persist` | Outbound Adapter | ✓ | ✓ |
| `boilerplate-adapter-output-cache` | Outbound Adapter | ✓ | ✗ |
| `boilerplate-boot-api` | Boot | ✓ | ✗ |

## 의존성 방향 규칙 [ADR-0001]

```
boilerplate-boot-api
  ├── boilerplate-application-autoconfiguration
  ├── boilerplate-adapter-input-api ──────────→ boilerplate-application
  ├── boilerplate-adapter-input-ws ───────────→ boilerplate-application
  ├── boilerplate-adapter-output-persist ─────→ boilerplate-application
  └── boilerplate-adapter-output-cache ───────→ boilerplate-application
                                                   │
                                                   ↓
                                           boilerplate-domain
```

### 금지 규칙 [ADR-0001]

1. **adapter ↔ adapter 상호 의존 전면 금지** — adapter 간 통신은 반드시 application 레이어(port)를 경유
2. **순환 의존 전면 금지**
3. **domain → 외부 금지** — domain은 순수 Java만 허용 (Spring/JPA 포함 모든 외부 라이브러리 금지)
4. **application → Spring 금지** — UseCase 구현체에 `@Service` 사용 불가; autoconfiguration에서 `@Bean` 등록
5. **query service → command outbound port 의존 금지** — CQRS 경계 유지

## 패키지 구조 규칙 [ADR-0003]

```
io.github.ppzxc.boilerplate
├── domain/                          ← boilerplate-domain 모듈
├── application/                     ← boilerplate-application 모듈
│   ├── port/
│   │   ├── input/
│   │   │   ├── command/   *UseCase  ← Inbound Command Port (interface)
│   │   │   └── query/     *Query    ← Inbound Query Port (interface)
│   │   └── output/
│   │       ├── command/   *Port     ← Outbound Command Port (interface)
│   │       ├── query/     *Port     ← Outbound Query Port (interface)
│   │       └── shared/    *Port     ← Shared Infra Port (interface)
│   └── service/
│       ├── command/       *Service  ← Command UseCase 구현체
│       └── query/         *Service  ← Query UseCase 구현체
├── adapter/input/api/               ← boilerplate-adapter-input-api 모듈
├── adapter/input/ws/                ← boilerplate-adapter-input-ws 모듈
├── adapter/output/persist/          ← boilerplate-adapter-output-persist 모듈
└── adapter/output/cache/            ← boilerplate-adapter-output-cache 모듈
```

## Port 인터페이스 규칙 [ADR-0003]

- `..port.output..*` — 반드시 interface
- `..port.input.command..*UseCase` — 반드시 interface
- `..port.input.query..*Query` — 반드시 interface

## ArchUnit 강제 검증 [ADR-0004]

다음 테스트는 항상 통과해야 한다. 위반 시 빌드 실패.

| 테스트 클래스 | 모듈 | 검증 규칙 |
|-------------|------|----------|
| `DomainArchitectureTest` | `boilerplate-domain` | Spring/JPA 금지, 허용 패키지 |
| `ApplicationArchitectureTest` | `boilerplate-application` | Spring 금지, Port interface, query→command port 금지 |

새 레이어 규칙 추가 시 해당 ArchUnit 테스트도 함께 추가.

## 비즈니스 로직 위치 규칙 [ADR-0001]

- 비즈니스 로직은 domain/application 레이어에서만 구현할 것 — adapter는 변환/위임만 담당

## 트랜잭션 관리 규칙 [ADR-0012]

- 트랜잭션 경계는 `boilerplate-application-autoconfiguration`에서 데코레이터로 적용할 것
- Command UseCase Bean: `@Transactional`, Query UseCase Bean: `@Transactional(readOnly = true)`
- application 레이어에서 Spring 트랜잭션 직접 사용 금지

## 에러 처리 레이어 규칙 [ADR-0007]

- domain/application 레이어: 순수 Java 예외만 던짐
- adapter 레이어(boilerplate-adapter-input-api): 예외를 `ProblemDetail`로 변환

## 관측성 의존성 배치 규칙 [ADR-0008]

- Actuator, OpenTelemetry 의존성: `boilerplate-boot-api` 모듈에 배치
- 로깅 설정 파일 (`logback-spring.xml`, `application.yml`): `boilerplate-boot-api/src/main/resources/`

## 객체 변환 규칙 [ADR-0013]

- adapter 레이어의 외부 표현 ↔ 도메인 변환은 MapStruct Mapper를 사용할 것 [ADR-0013]
- domain/application 레이어에서 MapStruct 사용 금지 — static factory 또는 생성자로 변환할 것 [ADR-0013]
- Mapper 인터페이스는 해당 adapter 모듈에 배치할 것 — 다른 모듈에서 참조 금지 [ADR-0013]
- unmappedTargetPolicy는 ERROR로 설정하여 매핑 누락을 컴파일 시 감지할 것 [ADR-0013]

## 모듈 자동 조립 규칙 [ADR-0014]

규칙 상세는 `rules/assembly.md` 참조. 요약:
- 모든 모듈은 자체 AutoConfiguration으로 Bean을 등록할 것 [ADR-0014]
- Boot 모듈에서 `@ComponentScan` 커스터마이징 및 `@Bean` 직접 등록 금지 [ADR-0014]
