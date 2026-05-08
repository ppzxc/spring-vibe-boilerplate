package io.github.ppzxc.boilerplate.audit.adapter.output.persist;

import static io.github.ppzxc.boilerplate.audit.persistence.jooq.Tables.AUDIT_LOG;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.port.out.AuditLogQueryPort;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
public class AuditLogQueryAdapter implements AuditLogQueryPort {

  private final DSLContext dsl;

  public AuditLogQueryAdapter(DSLContext dsl) {
    this.dsl = Objects.requireNonNull(dsl, "dsl must not be null");
  }

  @Override
  public List<AuditLogSummary> findBySubjectUserId(String subjectUserId, int limit) {
    return dsl.selectFrom(AUDIT_LOG)
        .where(AUDIT_LOG.SUBJECT_USER_ID.eq(UUID.fromString(subjectUserId)))
        .orderBy(AUDIT_LOG.RECORDED_AT.desc())
        .limit(limit)
        .fetch(
            r ->
                new AuditLogSummary(
                    r.getId(),
                    r.getSubjectUserId(),
                    r.getEventType(),
                    r.getPayload().data(),
                    r.getOccurredAt().toInstant(),
                    r.getRecordedAt().toInstant()));
  }

  @Override
  public List<AuditLogSummary> findRecent(int limit) {
    return dsl.selectFrom(AUDIT_LOG)
        .orderBy(AUDIT_LOG.RECORDED_AT.desc())
        .limit(limit)
        .fetch(
            r ->
                new AuditLogSummary(
                    r.getId(),
                    r.getSubjectUserId(),
                    r.getEventType(),
                    r.getPayload().data(),
                    r.getOccurredAt().toInstant(),
                    r.getRecordedAt().toInstant()));
  }
}
