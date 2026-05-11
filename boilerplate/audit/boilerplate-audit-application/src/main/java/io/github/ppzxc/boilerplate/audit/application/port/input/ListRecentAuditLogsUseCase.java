package io.github.ppzxc.boilerplate.audit.application.port.input;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.ListRecentAuditLogsQuery;
import java.util.List;

/** 최근 감사 로그 목록을 조회하는 UseCase. */
public interface ListRecentAuditLogsUseCase {

  List<AuditLogSummary> execute(ListRecentAuditLogsQuery query);
}
