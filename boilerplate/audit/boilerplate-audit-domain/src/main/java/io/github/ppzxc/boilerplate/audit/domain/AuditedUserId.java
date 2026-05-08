package io.github.ppzxc.boilerplate.audit.domain;

import java.util.Objects;
import java.util.UUID;

/** Audit BC 내부에서 감사 대상 사용자를 나타내는 VO (ACL — Identity UserId와 의미 동일하나 BC 경계 내부 표현). */
public record AuditedUserId(UUID value) {

  public AuditedUserId {
    Objects.requireNonNull(value, "AuditedUserId must not be null");
  }
}
