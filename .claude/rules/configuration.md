# Configuration Rules

## 프로파일 구조 [ADR-0011]

파일 위치: `template-boot-api/src/main/resources/`

| 파일 | 용도 |
|------|------|
| `application.yml` | 공통 설정 + 환경변수 플레이스홀더 |
| `application-local.yml` | 로컬 개발 |
| `application-prod.yml` | 프로덕션 (환경변수 중심, 기본값 없음) |

## 환경변수 오버라이드 패턴

```yaml
# 올바른 패턴: 기본값 있는 비밀이 아닌 값
some-config:
  endpoint: ${SOME_ENDPOINT:http://localhost:8080}
  secret: ${SOME_SECRET}     # 비밀: 기본값 없음

# 선택적 설정 (빈 문자열 기본값)
management:
  otlp:
    tracing:
      endpoint: ${OTLP_ENDPOINT:}
```

## 비밀 관리 규칙

- 비밀(패스워드, API 키, 토큰)은 반드시 환경변수로 주입
- yml 파일에 비밀 평문 작성 금지 — `.gitignore` 여부와 무관
- 기본값 없는 패턴 사용: `${SECRET}` (기동 시 오류 발생, 의도적)
- `application-prod.yml`에는 비밀 기본값 절대 작성 금지

## 주요 환경변수 목록

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `local` |
| `OTLP_ENDPOINT` | OTel 수집 엔드포인트 | (없음, 선택) |

## 금지 패턴

- `@Value` 필드 주입 금지 — `@ConfigurationProperties` 사용
- 프로파일별 Bean 분기(`@Profile`) 남용 금지 — 설정값으로 제어
