package io.github.ppzxc.boilerplate.audit.domain;

import java.util.Objects;
import java.util.UUID;

/** 감사 로그 식별자 — UUIDv7 기반 (ADR-0011). */
public record AuditLogId(UUID value) {

  public AuditLogId {
    Objects.requireNonNull(value, "AuditLogId must not be null");
  }

  public static AuditLogId generate() {
    return new AuditLogId(UUIDv7.generate());
  }
}
