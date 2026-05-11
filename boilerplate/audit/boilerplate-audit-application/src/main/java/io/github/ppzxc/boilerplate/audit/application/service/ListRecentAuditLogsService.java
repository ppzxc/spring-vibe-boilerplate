package io.github.ppzxc.boilerplate.audit.application.service;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.ListRecentAuditLogsQuery;
import io.github.ppzxc.boilerplate.audit.application.port.input.ListRecentAuditLogsUseCase;
import io.github.ppzxc.boilerplate.audit.application.port.output.AuditLogQueryPort;
import java.util.List;
import java.util.Objects;

/** 최근 감사 로그 목록을 조회하는 Service. */
public class ListRecentAuditLogsService implements ListRecentAuditLogsUseCase {

  private final AuditLogQueryPort queryPort;

  public ListRecentAuditLogsService(AuditLogQueryPort queryPort) {
    this.queryPort = Objects.requireNonNull(queryPort, "AuditLogQueryPort must not be null");
  }

  @Override
  public List<AuditLogSummary> execute(ListRecentAuditLogsQuery query) {
    return queryPort.findRecent(query.limit());
  }
}
