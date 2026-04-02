# Rules Maintenance

## ADR ↔ Rules 동기화 원칙

ADR(`docs/decisions/NNNN-*.md`)을 새로 작성할 때:
1. 결정에서 **제약 조건**을 추출하여 관련 `.claude/rules/` 파일에 추가
2. 규칙 항목에 `[ADR-NNNN]` 태그 달기
3. ADR에는 근거(why)를 유지, rules에는 제약(what)만 작성

ADR 없이 규칙을 추가할 때:
- 근거가 비자명하면 ADR 먼저 작성
- 명백한 코딩 컨벤션은 ADR 없이 rules에 직접 추가 가능

## Rules 작성 기준

rules 파일은 **스켈레톤 가드레일**이다. 구현자가 매번 직접 작성해야 할 코드를 대신 지시하지 않는다.

**허용** (원칙/제약):
- `~할 것`, `~금지`, `~사용 금지` 형식의 한 문장
- 레이어 경계 규칙, 의존성 방향 규칙, 도구 사용 원칙
- 형식: `<원칙 한 문장> [ADR-NNNN]`

**금지** (구현 디테일):
- 클래스명, 메서드명, 어노테이션 이름
- 상속/구현 대상 지정 (`extends`, `implements`)
- yml/properties 설정 코드 블록 (boot 모듈 가드레일이 아닌 경우)
- JSON/Java 코드 예시 블록
- 파일 경로 지정 (모듈 배치 규칙 제외)

예시:
```
✓ adapter-input-api에서 ProblemDetail(RFC 9457)로 예외를 변환할 것 [ADR-0007]
✗ 클래스명: GlobalExceptionHandler, 어노테이션: @RestControllerAdvice, 상속: ResponseEntityExceptionHandler
```

## 규칙 파일 크기 관리

- 파일 하나당 **100줄 이하** 유지
- 100줄 초과 시 → 주제별로 파일 분리
- 예: `testing.md`가 커지면 `testing-unit.md`, `testing-integration.md`로 분리

## 규칙 정리 기준

| 상태 | 대응 |
|------|------|
| 같은 규칙이 두 파일에 중복 | 한 파일로 통합, 나머지에서 참조 |
| 6개월 이상 참조되지 않은 규칙 | 삭제 또는 `docs/decisions/`로 이동 |
| 규칙이 서로 모순 | ADR 작성 후 한쪽 삭제 |
| 규칙이 현재 코드와 불일치 | 코드 또는 규칙 중 하나를 맞춤 |

## MADR 작성 형식 [MADR 4.0]

`docs/decisions/NNNN-<kebab-case-title>.md` 형식으로 저장.
번호는 앞에 있는 최대 번호 + 1.

템플릿:
- full: `docs/decisions/0000-template.md` — 대부분의 결정에 사용
- minimal: `docs/decisions/0000-template-minimal.md` — 검토 옵션이 2개 이하이고 트레이드오프가 경미한 결정

frontmatter 필수 필드:
- `status`: accepted / deprecated / superseded by ADR-NNNN
- `date`: YYYY-MM-DD
- `decision-makers`: 결정 참여자

full 템플릿 필수 섹션:
- Context and Problem Statement
- Considered Options (최소 2개)
- Decision Outcome + Consequences

## CLAUDE.md 유지 원칙

- 항상 50줄 이하로 유지
- 세부 규칙은 CLAUDE.md에 쓰지 않고 `.claude/rules/`에 위임
- rules 파일이 추가되면 CLAUDE.md의 규칙 파일 목록 테이블 업데이트
- ADR 추가 시 `docs/decisions/README.md` 인덱스 테이블도 업데이트
