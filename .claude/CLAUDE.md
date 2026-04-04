# CLAUDE.md

## 이 프로젝트란?

Spring Boot 4 + Hexagonal Architecture 범용 보일러플레이트.
Java 25, Virtual Threads 기반.
베이스 패키지: `io.github.ppzxc.boilerplate`

## 모듈 구성 (8개)

```
boilerplate-domain                        # 순수 Java (Spring/JPA 금지)
boilerplate-application                   # 순수 Java (Spring 금지)
boilerplate-application-autoconfiguration # UseCase Bean 등록
boilerplate-adapter-input-api             # REST Controller + Security
boilerplate-adapter-input-ws              # WebSocket
boilerplate-adapter-output-persist        # Outbound Adapter (영속화 기술 선택)
boilerplate-adapter-output-cache          # Cache
boilerplate-boot-api                      # Spring Boot 앱 (port 8080)
```

## 빠른 참조

```bash
./gradlew spotlessApply       # 포맷 수정 (코드 작성 후 필수)
./gradlew compileJava         # 컴파일 (ErrorProne + NullAway 포함)
./gradlew test                # 전체 테스트
./gradlew bootRun             # 로컬 실행
```

## 규칙 파일

프로젝트 규칙의 정본은 이 파일(`CLAUDE.md`), `.claude/rules/*`, `docs/decisions/*` 세 경로뿐이다.
작업 전 관련 규칙 파일을 읽는다.

| 작업 | 파일 |
|------|------|
| 아키텍처 / 레이어 작업 | `rules/architecture.md` |
| 새 모듈 추가 | `rules/module-add.md` |
| 테스트 작성 | `rules/testing.md` |
| 코드 작성 / 네이밍 | `rules/coding-style.md` |
| CI 도구 사용 | `rules/ci-tools.md` |
| ADR 작성 / 규칙 수정 | `rules/rules-maintenance.md` |
| 에러 처리 | `rules/error-handling.md` |
| 관측성 (로깅, Actuator, OTel) | `rules/observability.md` |
| API 문서화 (springdoc, Redoc, Springwolf) | `rules/api-documentation.md` |
| 컨테이너화 / 배포 | `rules/containerization.md` |
| 환경 설정 (프로파일, 환경변수) | `rules/configuration.md` |
| Bean 조립 / AutoConfiguration | `rules/assembly.md` |
| 멀티테넌시 (테넌트 격리, RLS) | `rules/multi-tenancy.md` |

결정의 근거는 [`docs/decisions/README.md`](../docs/decisions/README.md) 참조. 규칙 파일의 `[ADR-NNNN]` 태그가 해당 ADR을 가리킨다.
