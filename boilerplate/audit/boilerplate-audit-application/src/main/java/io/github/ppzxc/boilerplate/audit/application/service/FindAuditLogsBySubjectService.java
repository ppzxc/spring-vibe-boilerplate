package io.github.ppzxc.boilerplate.audit.application.service;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.FindAuditLogsBySubjectQuery;
import io.github.ppzxc.boilerplate.audit.application.port.in.FindAuditLogsBySubjectUseCase;
import io.github.ppzxc.boilerplate.audit.application.port.out.AuditLogQueryPort;
import java.util.List;
import java.util.Objects;

/** 특정 사용자의 감사 로그를 조회하는 Service. */
public class FindAuditLogsBySubjectService implements FindAuditLogsBySubjectUseCase {

  private final AuditLogQueryPort queryPort;

  public FindAuditLogsBySubjectService(AuditLogQueryPort queryPort) {
    this.queryPort = Objects.requireNonNull(queryPort, "AuditLogQueryPort must not be null");
  }

  @Override
  public List<AuditLogSummary> execute(FindAuditLogsBySubjectQuery query) {
    return queryPort.findBySubjectUserId(query.subjectUserId(), query.limit());
  }
}
