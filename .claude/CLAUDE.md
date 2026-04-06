# CLAUDE.md

@../AGENTS.md

## Claude 전용 규칙 파일

프로젝트 규칙의 정본은 `AGENTS.md`, `.claude/rules/*`, `docs/decisions/*` 세 경로뿐이다.
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
