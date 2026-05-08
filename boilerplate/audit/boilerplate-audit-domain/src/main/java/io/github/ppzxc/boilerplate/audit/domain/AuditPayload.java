package io.github.ppzxc.boilerplate.audit.domain;

import java.util.Objects;

/** 감사 페이로드 — JSONB에 저장되는 raw JSON 문자열 (1~10000자). JSON 문법 검증 X. */
public record AuditPayload(String value) {

  public AuditPayload {
    Objects.requireNonNull(value, "AuditPayload must not be null");
    if (value.isBlank() || value.length() > 10000) {
      throw new IllegalArgumentException(
          "AuditPayload must be between 1 and 10000 characters: length=" + value.length());
    }
  }
}
