package io.github.ppzxc.boilerplate.audit.application.port.input;

import io.github.ppzxc.boilerplate.audit.application.dto.RecordUserRegisteredAuditCommand;

/** UserRegistered 이벤트를 감사 로그로 기록하는 UseCase. */
public interface RecordUserRegisteredAuditUseCase {

  void execute(RecordUserRegisteredAuditCommand command);
}
