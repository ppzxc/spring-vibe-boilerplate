# Architecture Rules

## 모듈 레이아웃 [ADR-0002]

8개 모듈, `template/` 하위 플랫 구조.

| 모듈 | 레이어 | Spring | JPA |
|------|--------|--------|-----|
| `template-domain` | Domain | ✗ | ✗ |
| `template-application` | Application | ✗ | ✗ |
| `template-application-autoconfiguration` | Application | ✓ | ✗ |
| `template-adapter-input-api` | Inbound Adapter | ✓ | ✗ |
| `template-adapter-input-ws` | Inbound Adapter | ✓ | ✗ |
| `template-adapter-output-persist` | Outbound Adapter | ✓ | ✓ |
| `template-adapter-output-cache` | Outbound Adapter | ✓ | ✗ |
| `template-boot-api` | Boot | ✓ | ✗ |

## 의존성 방향 규칙 [ADR-0001]

```
template-boot-api
  ├── template-application-autoconfiguration
  ├── template-adapter-input-api ──────────→ template-application
  ├── template-adapter-input-ws ───────────→ template-application
  ├── template-adapter-output-persist ─────→ template-application
  └── template-adapter-output-cache ───────→ template-application
                                                   │
                                                   ↓
                                           template-domain
```

### 금지 규칙 [ADR-0001]

1. **adapter ↔ adapter 상호 의존 전면 금지** — adapter 간 통신은 반드시 application 레이어(port)를 경유
2. **순환 의존 전면 금지**
3. **domain → 외부 금지** — domain은 순수 Java만 허용 (Spring/JPA 포함 모든 외부 라이브러리 금지)
4. **application → Spring 금지** — UseCase 구현체에 `@Service` 사용 불가; autoconfiguration에서 `@Bean` 등록
5. **query service → command outbound port 의존 금지** — CQRS 경계 유지

## 패키지 구조 규칙 [ADR-0003]

```
io.github.ppzxc.template
├── domain/                          ← template-domain 모듈
├── application/                     ← template-application 모듈
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
├── adapter/input/api/               ← template-adapter-input-api 모듈
├── adapter/input/ws/                ← template-adapter-input-ws 모듈
├── adapter/output/persist/          ← template-adapter-output-persist 모듈
└── adapter/output/cache/            ← template-adapter-output-cache 모듈
```

## Port 인터페이스 규칙 [ADR-0003]

- `..port.output..*` — 반드시 interface
- `..port.input.command..*UseCase` — 반드시 interface
- `..port.input.query..*Query` — 반드시 interface

## ArchUnit 강제 검증 [ADR-0004]

다음 테스트는 항상 통과해야 한다. 위반 시 빌드 실패.

| 테스트 클래스 | 모듈 | 검증 규칙 |
|-------------|------|----------|
| `DomainArchitectureTest` | `template-domain` | Spring/JPA 금지, 허용 패키지 |
| `ApplicationArchitectureTest` | `template-application` | Spring 금지, Port interface, query→command port 금지 |

새 레이어 규칙 추가 시 해당 ArchUnit 테스트도 함께 추가.

## 에러 처리 레이어 규칙 [ADR-0007]

- domain/application 레이어: 순수 Java 예외만 던짐
- adapter 레이어(template-adapter-input-api): 예외를 `ProblemDetail`로 변환

## 관측성 의존성 배치 규칙 [ADR-0008]

- Actuator, OpenTelemetry 의존성: `template-boot-api` 모듈에 배치
- 로깅 설정 파일 (`logback-spring.xml`, `application.yml`): `template-boot-api/src/main/resources/`
