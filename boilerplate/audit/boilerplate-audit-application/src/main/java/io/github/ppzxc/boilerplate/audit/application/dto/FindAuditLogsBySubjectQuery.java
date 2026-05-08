package io.github.ppzxc.boilerplate.audit.application.dto;

import java.util.Objects;

/** 특정 사용자의 감사 로그 조회 Query. */
public record FindAuditLogsBySubjectQuery(String subjectUserId, int limit) {

  public FindAuditLogsBySubjectQuery {
    Objects.requireNonNull(subjectUserId, "subjectUserId must not be null");
    if (subjectUserId.isBlank()) {
      throw new IllegalArgumentException("subjectUserId must not be blank");
    }
    if (limit < 1 || limit > 1000) {
      throw new IllegalArgumentException("limit must be between 1 and 1000: " + limit);
    }
  }
}
