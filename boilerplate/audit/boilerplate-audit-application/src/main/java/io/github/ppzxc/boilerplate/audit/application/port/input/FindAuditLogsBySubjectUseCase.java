package io.github.ppzxc.boilerplate.audit.application.port.input;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.FindAuditLogsBySubjectQuery;
import java.util.List;

/** 특정 사용자의 감사 로그를 조회하는 UseCase. */
public interface FindAuditLogsBySubjectUseCase {

  List<AuditLogSummary> execute(FindAuditLogsBySubjectQuery query);
}
