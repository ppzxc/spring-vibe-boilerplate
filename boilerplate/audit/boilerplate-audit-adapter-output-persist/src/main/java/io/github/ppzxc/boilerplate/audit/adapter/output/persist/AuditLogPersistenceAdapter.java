package io.github.ppzxc.boilerplate.audit.adapter.output.persist;

import static io.github.ppzxc.boilerplate.audit.persistence.jooq.Tables.AUDIT_LOG;

import io.github.ppzxc.boilerplate.audit.application.port.output.LoadAuditLogPort;
import io.github.ppzxc.boilerplate.audit.application.port.output.SaveAuditLogPort;
import io.github.ppzxc.boilerplate.audit.domain.AuditLog;
import io.github.ppzxc.boilerplate.audit.domain.AuditLogId;
import java.util.Objects;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceAdapter implements LoadAuditLogPort, SaveAuditLogPort {

  private final DSLContext dsl;
  private final ApplicationEventPublisher eventPublisher;

  public AuditLogPersistenceAdapter(DSLContext dsl, ApplicationEventPublisher eventPublisher) {
    this.dsl = Objects.requireNonNull(dsl, "dsl must not be null");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
  }

  @Override
  public Optional<AuditLog> findById(AuditLogId id) {
    return dsl.selectFrom(AUDIT_LOG)
        .where(AUDIT_LOG.ID.eq(id.value()))
        .fetchOptional(AuditLogPersistenceMapper::toDomain);
  }

  @Override
  public void save(AuditLog entity) {
    var record = AuditLogPersistenceMapper.toRecord(entity);
    dsl.insertInto(AUDIT_LOG)
        .set(AUDIT_LOG.ID, record.getId())
        .set(AUDIT_LOG.SUBJECT_USER_ID, record.getSubjectUserId())
        .set(AUDIT_LOG.EVENT_TYPE, record.getEventType())
        .set(AUDIT_LOG.PAYLOAD, record.getPayload())
        .set(AUDIT_LOG.OCCURRED_AT, record.getOccurredAt())
        .set(AUDIT_LOG.RECORDED_AT, record.getRecordedAt())
        .set(AUDIT_LOG.VERSION, record.getVersion())
        .execute();

    // append-only — 이벤트 없음 (항상 빈 목록), Adapter 코드 일관성 유지 (AD-3)
    entity.pullDomainEvents().forEach(eventPublisher::publishEvent);
  }
}
