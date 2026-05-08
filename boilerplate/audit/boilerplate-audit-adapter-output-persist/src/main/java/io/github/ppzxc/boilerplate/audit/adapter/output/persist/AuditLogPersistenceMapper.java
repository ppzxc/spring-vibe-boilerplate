package io.github.ppzxc.boilerplate.audit.adapter.output.persist;

import io.github.ppzxc.boilerplate.audit.domain.AuditEventType;
import io.github.ppzxc.boilerplate.audit.domain.AuditLog;
import io.github.ppzxc.boilerplate.audit.domain.AuditLogId;
import io.github.ppzxc.boilerplate.audit.domain.AuditPayload;
import io.github.ppzxc.boilerplate.audit.domain.AuditedUserId;
import io.github.ppzxc.boilerplate.audit.persistence.jooq.tables.records.AuditLogRecord;
import java.time.ZoneOffset;

/** Domain ↔ jOOQ Record 변환 매퍼 (AD-4). */
public final class AuditLogPersistenceMapper {

  private AuditLogPersistenceMapper() {}

  public static AuditLog toDomain(AuditLogRecord record) {
    return AuditLog.reconstitute(
        new AuditLogId(record.getId()),
        new AuditedUserId(record.getSubjectUserId()),
        AuditEventType.valueOf(record.getEventType()),
        new AuditPayload(record.getPayload().data()),
        record.getOccurredAt().toInstant(),
        record.getRecordedAt().toInstant(),
        record.getVersion());
  }

  public static AuditLogRecord toRecord(AuditLog entity) {
    var record = new AuditLogRecord();
    record.setId(entity.id().value());
    record.setSubjectUserId(entity.subjectUserId().value());
    record.setEventType(entity.eventType().name());
    record.setPayload(org.jooq.JSON.valueOf(entity.payload().value()));
    record.setOccurredAt(entity.occurredAt().atOffset(ZoneOffset.UTC));
    record.setRecordedAt(entity.recordedAt().atOffset(ZoneOffset.UTC));
    record.setVersion(entity.version());
    return record;
  }
}
