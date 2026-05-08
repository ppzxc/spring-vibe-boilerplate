# Ubiquitous Language — Identity BC

> 도메인 전문가와 개발자가 공유하는 Identity BC의 용어 사전.
> 코드 식별자(클래스명, 메서드명)에 그대로 반영한다.
>
> 신규 용어 추가/변경 시 `strategic-design-changelog.md`에 기록한다.
> UL 충돌 검사: 신규 BC 추가 시 이 파일의 용어와 신규 BC 용어 간 충돌 여부를 확인한다.

---

| 용어(한국어) | 코드 식별자 | 정의 | 유사 용어와의 차이 |
|------------|-----------|------|-----------------|
| 사용자 | `User` | 시스템에 등록된 식별 가능한 주체 | `Member`(팀 소속 관계)와 다름 |
| 사용자 ID | `UserId` | UUIDv7 식별자 VO | 단순 `UUID`와 구분 — VO 래핑, 타입 안전성 보장 |
| 이메일 | `Email` | 로그인 식별자 겸 알림 채널 | 단순 `String` 아님 — RFC 5322 형식 검증 |
| 사용자 이름 | `UserName` | 표시용 이름 (1-50자 제약) | Identifier 아님, Display only |
| 해시된 비밀번호 | `HashedPassword` | BCrypt/Argon2 해시 결과 | 평문 Password는 도메인에 절대 존재하지 않음 |
| 자격증명 | `Credential` | User Aggregate 내부 Entity (해시된 비밀번호 + 만료 정보 포함) | Aggregate Root는 `User`, `Credential`은 종속 Entity |
| 사용자 상태 | `UserStatus` | `ACTIVE` / `SUSPENDED` / `DEACTIVATED` enum | 단방향 전이만 허용 (DEACTIVATED → ACTIVE 불가) |
| 권한 | `Permission` | `resource:scope` 형식 VO (예: `user:create`) | OAuth2 `Scope`와 다름 — Permission은 최소 인가 단위 |
| 역할 | `Role` | `Permission` 집합 Aggregate | `Group`(조직 단위)과 다름 — 순수 권한 집합 |
| 클라이언트 | `Client` | OAuth2 클라이언트 애플리케이션 | `User`(사람)와 구분 — M2M 토큰 발급 주체 |
