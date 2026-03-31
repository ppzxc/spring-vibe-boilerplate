# Architecture Rules

## 레이어 의존성 제약 [ADR-0001]

### template-domain (순수 도메인)
- `org.springframework..` 의존 금지
- `jakarta.persistence..` 의존 금지
- 허용 패키지: `io.github.ppzxc.template.domain..`, `io.github.ppzxc.template.common..`,
  `java..`, `javax..`, `jakarta.validation..`, `lombok..`

### template-application (UseCase + Port)
- `org.springframework..` 의존 금지
- 허용 패키지: `io.github.ppzxc.template.application..`, `io.github.ppzxc.template.domain..`,
  `io.github.ppzxc.template.common..`, `java..`, `javax..`

### Port 인터페이스 규칙
- `..port.out..*` 클래스는 반드시 interface
- `..port.in.command..*UseCase` 클래스는 반드시 interface
- `..port.in.query..*Query` 클래스는 반드시 interface
- Query 서비스(`..service.query..`)는 Command 포트(`..port.out.command..`) 의존 금지

## 모듈 레이아웃 [ADR-0001]

모든 모듈은 `template/` 하위 flat layout.

| Prefix | 역할 |
|--------|------|
| `template-domain` | 순수 도메인 모델 |
| `template-application` | UseCase + Port 인터페이스 |
| `template-application-autoconfiguration` | UseCase Bean 등록 |
| `template-adapter-input-*` | Inbound Adapter (Web, WS 등) |
| `template-adapter-output-*` | Outbound Adapter (DB, Cache, 외부 서비스) |
| `template-boot-*` | 실행 가능한 Spring Boot 앱 |
| `template-common` | 공통 유틸리티 (Spring 의존 허용) |

## ArchUnit 강제 검증

다음 테스트는 항상 통과해야 한다. 위반 시 빌드 실패.

- `DomainArchitectureTest` — `template-domain` 모듈
- `ApplicationArchitectureTest` — `template-application` 모듈

새 레이어 규칙 추가 시 해당 ArchUnit 테스트도 함께 추가한다.
