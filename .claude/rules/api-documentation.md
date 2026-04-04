# API Documentation Rules

## 의존성 위치 규칙 [ADR-0009]

| 도구 | 모듈 |
|------|------|
| springdoc-openapi | `boilerplate-adapter-input-api` |
| Redoc HTML | `boilerplate-adapter-input-api` |
| Springwolf | `boilerplate-adapter-input-ws` |

## springdoc 설정 규칙

```yaml
# boilerplate-boot-api/src/main/resources/application.yml
springdoc:
  swagger-ui:
    enabled: false       # Swagger UI 비활성화 (Redoc 사용)
  api-docs:
    path: /v3/api-docs
```

- Swagger UI 비활성화 필수 — UI는 Redoc으로 통일
- API 문서 접근 URL: `/v3/api-docs`
- Redoc 접근 URL: `/redoc.html`

## Controller 어노테이션 규칙 [ADR-0009]

- REST API 문서는 Redoc으로 제공할 것 (Swagger UI 비활성화)
- Controller에 @Tag, @Operation, @ApiResponse를 필수 적용할 것

## WebSocket 문서화 규칙 [ADR-0009]

- WebSocket 엔드포인트는 Springwolf로 문서화할 것 (@AsyncListener, @AsyncPublisher 적용)
