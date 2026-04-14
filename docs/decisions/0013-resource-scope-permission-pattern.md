# ADR-0013: resource:scope Permission 패턴

## Status

Accepted

## Context

인가 시스템에서 Permission을 문자열로 정의할 때 일관된 네이밍 규칙이 없으면 `"ROLE_ADMIN"`, `"canEditUser"`, `"user_edit"` 등 혼재가 발생한다. OAauth2 Scope와 세밀한 Permission을 구분해야 한다.

## Decision

Permission은 **`{resource}:{scope}`** 형식으로 정의한다.

```
resource = 도메인 리소스 타입 (소문자, 단수형)
scope    = 행위 (소문자, 동사형)
예: user:view, user:create, role:assign, token:revoke
```

**금지 패턴**:
- `resource:scope:instanceId` — Instance-Level 권한은 Domain 비즈니스 규칙으로 처리
- Permission에 테넌트 ID 포함 — 테넌트는 `TenantId`로 격리

**Token Intersection**:
```
grantedPermissions = intersect(client.scopePermissions(), user.rolePermissions())
```

## Consequences

Permission 네이밍이 일관적이고 기계적으로 검증 가능. OAuth2 Scope(앱용 거친 제어)와 Permission(사람용 세밀한 제어)이 명확히 분리됨. Instance-Level 인가는 Domain 소유권 검증으로 처리해야 하는 제약.
