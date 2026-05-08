package io.github.ppzxc.boilerplate.audit.application.service;

import io.github.ppzxc.boilerplate.audit.application.dto.RecordUserRegisteredAuditCommand;
import io.github.ppzxc.boilerplate.audit.application.port.in.RecordUserRegisteredAuditUseCase;
import io.github.ppzxc.boilerplate.audit.application.port.out.SaveAuditLogPort;
import io.github.ppzxc.boilerplate.audit.domain.AuditEventType;
import io.github.ppzxc.boilerplate.audit.domain.AuditLog;
import io.github.ppzxc.boilerplate.audit.domain.AuditPayload;
import io.github.ppzxc.boilerplate.audit.domain.AuditedUserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** UserRegistered 이벤트를 감사 로그로 기록하는 Service. Spring 의존 없이 순수 Java로 JSON 빌드. */
public class RecordUserRegisteredAuditService implements RecordUserRegisteredAuditUseCase {

  private final SaveAuditLogPort savePort;
  private final Clock clock;

  public RecordUserRegisteredAuditService(SaveAuditLogPort savePort, Clock clock) {
    this.savePort = Objects.requireNonNull(savePort, "SaveAuditLogPort must not be null");
    this.clock = Objects.requireNonNull(clock, "Clock must not be null");
  }

  @Override
  public void execute(RecordUserRegisteredAuditCommand command) {
    var subjectUserId = new AuditedUserId(UUID.fromString(command.subjectUserId()));
    var occurredAt = Instant.parse(command.occurredAt());
    var recordedAt = clock.instant();
    var payload = new AuditPayload(buildPayloadJson(command.userName(), command.email()));

    var auditLog =
        AuditLog.create(
            subjectUserId, AuditEventType.USER_REGISTERED, payload, occurredAt, recordedAt);
    savePort.save(auditLog);
  }

  private static String buildPayloadJson(String userName, String email) {
    return "{\"userName\":\""
        + jsonEscape(userName)
        + "\",\"email\":\""
        + jsonEscape(email)
        + "\"}";
  }

  static String jsonEscape(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
