package io.github.ppzxc.boilerplate.audit.application.dto;

/** 최근 감사 로그 목록 조회 Query. */
public record ListRecentAuditLogsQuery(int limit) {

  public ListRecentAuditLogsQuery {
    if (limit < 1 || limit > 1000) {
      throw new IllegalArgumentException("limit must be between 1 and 1000: " + limit);
    }
  }
}
