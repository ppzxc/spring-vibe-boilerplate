package io.github.ppzxc.boilerplate.audit.application.port.out;

import io.github.ppzxc.boilerplate.audit.domain.AuditLog;
import io.github.ppzxc.boilerplate.audit.domain.AuditLogId;
import java.util.Optional;

/** 감사 로그 단건 조회 Output Port (Command Side). */
public interface LoadAuditLogPort {

  Optional<AuditLog> findById(AuditLogId id);
}
