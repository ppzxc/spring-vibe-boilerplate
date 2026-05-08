package io.github.ppzxc.boilerplate.audit.application.port.out;

import io.github.ppzxc.boilerplate.audit.domain.AuditLog;

/** 감사 로그 저장 Output Port (Command Side). */
public interface SaveAuditLogPort {

  void save(AuditLog auditLog);
}
