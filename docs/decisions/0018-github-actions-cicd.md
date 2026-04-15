# ADR-0018: GitHub Actions CI/CD 파이프라인 채택

## Status

Accepted

## Context

CI/CD 플랫폼 선택지:
- **Jenkins**: 자체 호스팅, 플러그인 관리 오버헤드, 인프라 비용.
- **GitLab CI**: GitLab 종속성.
- **GitHub Actions** (채택): 코드와 동일 플랫폼, 무료 공개 리포지토리, Marketplace 생태계, OIDC 기반 권한 관리.

이 프로젝트는 GitHub에서 호스팅되므로 GitHub Actions가 최적이다.

CI 필수 게이트: 컴파일, 테스트, 커버리지, 코드 품질, 정적 분석, Modulith 구조 검증, 보안 스캔(7개).

## Decision

GitHub Actions를 CI/CD 플랫폼으로 채택한다.

- CI 워크플로우: `.github/workflows/ci.yml` — PR/push 트리거, 5단계 게이트
- CD 워크플로우: `.github/workflows/cd.yml` — main push 시 Jib 이미지 빌드/푸시
- Release 워크플로우: `.github/workflows/release.yml` — `v*` 태그 트리거, GitHub Release 자동 생성

Gradle `--no-daemon` 필수 (메모리 누수 방지).
NVD API Key를 GitHub Secrets에 등록 (OWASP 스캔 속도 향상).

## Consequences

- ✅ 코드와 CI 설정이 동일 리포지토리 → 코드 리뷰 시 CI 변경도 검토 가능
- ✅ OIDC 기반 클라우드 연동 → 장기 크리덴셜 불필요
- ✅ GitHub Marketplace 풍부한 Actions 생태계
- ⚠️ GitHub 종속성 → 대안 이전 시 워크플로우 재작성 필요
- ⚠️ 월별 무료 사용량 한도 (공개 리포: 무제한, 비공개: 2,000분/월)
