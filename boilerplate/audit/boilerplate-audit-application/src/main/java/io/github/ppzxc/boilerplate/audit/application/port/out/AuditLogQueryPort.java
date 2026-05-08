package io.github.ppzxc.boilerplate.audit.application.port.out;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import java.util.List;

/** 감사 로그 복합 조회 Output Port (Query Side). */
public interface AuditLogQueryPort {

  List<AuditLogSummary> findBySubjectUserId(String subjectUserId, int limit);

  List<AuditLogSummary> findRecent(int limit);
}
