# API Documentation Rules

## 의존성 위치 규칙 [ADR-0009]

| 도구 | 모듈 |
|------|------|
| springdoc-openapi | `template-adapter-input-api` |
| Redoc HTML | `template-adapter-input-api/src/main/resources/static/redoc.html` |
| Springwolf | `template-adapter-input-ws` |

## springdoc 설정 규칙

```yaml
# application.yml
springdoc:
  swagger-ui:
    enabled: false       # Swagger UI 비활성화 (Redoc 사용)
  api-docs:
    path: /v3/api-docs
```

- Swagger UI 비활성화 필수 — UI는 Redoc으로 통일
- API 문서 접근 URL: `/v3/api-docs`
- Redoc 접근 URL: `/redoc.html`

## Controller 어노테이션 규칙

| 어노테이션 | 적용 위치 | 필수 여부 |
|-----------|----------|---------|
| `@Tag(name = "...")` | Controller 클래스 | 필수 |
| `@Operation(summary = "...")` | 핸들러 메서드 | 필수 |
| `@ApiResponse(responseCode = "200", description = "...")` | 핸들러 메서드 | 각 응답 코드마다 |

```java
@Tag(name = "Orders")
@RestController
public class OrderController {

    @Operation(summary = "주문 생성")
    @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderResponse> create(...) { ... }
}
```

## WebSocket 문서화 규칙 (Springwolf)

- `@AsyncListener` 어노테이션: consumer 엔드포인트 문서화
- `@AsyncPublisher` 어노테이션: publisher 엔드포인트 문서화
- AsyncAPI 접근 URL: `/springwolf/asyncapi-ui.html`

## 비고

- Spring Boot 4 호환성은 구현 시 검증 (springdoc, Springwolf 버전 확인 필요)
- Swagger UI 대신 Redoc을 사용하는 이유: 더 나은 가독성, 단일 HTML 파일 배포
