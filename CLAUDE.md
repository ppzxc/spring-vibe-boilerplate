# CLAUDE.md

## 금지 사항

- `docs/superpowers/*` 경로의 문서는 읽지 않는다.

## 이 프로젝트란?

Spring Boot 4 + Hexagonal Architecture 보일러플레이트.
Java 25, Virtual Threads, PostgreSQL 18 기반.
베이스 패키지: `io.github.ppzxc.template`

## 규칙 파일

작업 전 관련 규칙 파일을 읽는다.

| 작업 | 파일 |
|------|------|
| 아키텍처 / 레이어 작업 | `.claude/rules/architecture.md` |
| 새 모듈 추가 | `.claude/rules/module-add.md` |
| 테스트 작성 | `.claude/rules/testing.md` |
| 코드 작성 / 네이밍 | `.claude/rules/coding-style.md` |
| ADR 작성 / 규칙 수정 | `.claude/rules/rules-maintenance.md` |

## ADR 참조

결정의 근거가 필요하면 `docs/decisions/NNNN-*.md` 를 직접 읽는다.
규칙 파일의 `[ADR-NNNN]` 태그가 해당 ADR 번호를 가리킨다.
