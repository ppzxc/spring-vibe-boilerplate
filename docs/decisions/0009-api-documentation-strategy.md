---
status: accepted
date: 2026-04-01
decision-makers: ppzxc
---

# API 문서화 전략: springdoc-openapi + Redoc (REST) + Springwolf + AsyncAPI (WS)

## 배경 및 문제

REST API와 WebSocket 메시지 프로토콜 모두 문서화가 필요하다.
REST는 OpenAPI 3.x 스펙이 사실상 표준이지만, 렌더링 도구의 UX가 개발자 경험에 큰 영향을 준다.
WebSocket 메시지 계약은 REST와 별도의 문서화 체계가 필요하며,
AsyncAPI가 CNCF 프로젝트로 비동기 API의 표준으로 자리잡았다.

## 결정 기준

* REST API: OpenAPI 3.x 자동 생성 + 고품질 렌더링
* WebSocket: 메시지 계약 문서화
* 외부 서비스(Swagger Hub 등) 없이 자체 호스팅
* Spring Boot 4 호환성

## 결정

**springdoc-openapi + Redoc (REST) + Springwolf + AsyncAPI (WS)** 조합을 채택한다.

### 1. REST API 문서화

핵심 제약:

1. **OpenAPI 생성**: springdoc-openapi가 `/v3/api-docs` JSON 자동 생성
2. **렌더링**: Redoc static HTML 1개 파일로 통합 (`/docs` 경로)
3. **Swagger UI 비활성화**: `springdoc.swagger-ui.enabled: false`
4. **의존성 위치**: springdoc 의존성 + Redoc HTML은 `template-adapter-input-api` 모듈 (Controller와 응집)
5. **어노테이션 필수**: Controller에 `@Operation`, `@ApiResponse` 어노테이션 작성
6. **Spring Boot 4 호환성**: 구현 시 springdoc 버전 호환성 확인 필요

### 2. WebSocket 문서화

7. **도구**: Springwolf로 AsyncAPI 스펙 자동 생성
8. **스펙 버전**: AsyncAPI 3.0 (2023 stable)
9. **의존성 위치**: `template-adapter-input-ws` 모듈

## 검토한 대안

| 대안 | 미채택 이유 |
|------|-----------|
| springdoc + Swagger UI | Swagger UI는 try-it 기능 있으나 디자인 품질 낮음, 번들 크기 큼 |
| springdoc + Redoc (WS 없음) | WebSocket 메시지 계약 문서화 불가 |
| Spring REST Docs | 테스트 기반으로 유지 비용 높음, OpenAPI 자동 생성보다 수동 작업 많음 |
| Stoplight Elements | 외부 CDN 의존, 자체 호스팅 복잡 |

## 관련 문서

→ [architecture.md](../../.claude/rules/architecture.md) — 모듈 레이아웃 (adapter-input-api, adapter-input-ws)
→ [ADR-0002](0002-flat-module-structure.md) — 모듈 구성 원칙
→ [ADR-0003](0003-package-structure-and-naming.md) — Controller 패키지 구조
