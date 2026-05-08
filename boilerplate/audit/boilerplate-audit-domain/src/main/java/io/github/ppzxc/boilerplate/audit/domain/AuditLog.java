package io.github.ppzxc.boilerplate.audit.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 감사 로그 Aggregate Root — append-only, INSERT만 (상태 전이 없음). Domain Event는 발행하지 않음 (이 Aggregate 자체가
 * 이벤트 소비 결과).
 */
public final class AuditLog {

  private final AuditLogId id;
  private final AuditedUserId subjectUserId;
  private final AuditEventType eventType;
  private final AuditPayload payload;
  private final Instant occurredAt;
  private final Instant recordedAt;
  private final long version;
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private AuditLog(
      AuditLogId id,
      AuditedUserId subjectUserId,
      AuditEventType eventType,
      AuditPayload payload,
      Instant occurredAt,
      Instant recordedAt,
      long version) {
    this.id = Objects.requireNonNull(id, "AuditLogId must not be null");
    this.subjectUserId = Objects.requireNonNull(subjectUserId, "AuditedUserId must not be null");
    this.eventType = Objects.requireNonNull(eventType, "AuditEventType must not be null");
    this.payload = Objects.requireNonNull(payload, "AuditPayload must not be null");
    this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt must not be null");
    this.version = version;
  }

  public static AuditLog create(
      AuditedUserId subjectUserId,
      AuditEventType eventType,
      AuditPayload payload,
      Instant occurredAt,
      Instant recordedAt) {
    return new AuditLog(
        AuditLogId.generate(), subjectUserId, eventType, payload, occurredAt, recordedAt, 0L);
  }

  public static AuditLog reconstitute(
      AuditLogId id,
      AuditedUserId subjectUserId,
      AuditEventType eventType,
      AuditPayload payload,
      Instant occurredAt,
      Instant recordedAt,
      long version) {
    return new AuditLog(id, subjectUserId, eventType, payload, occurredAt, recordedAt, version);
  }

  /** Adapter 코드 일관성을 위해 보유하나 항상 빈 목록 반환. */
  public List<DomainEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  public AuditLogId id() {
    return id;
  }

  public AuditedUserId subjectUserId() {
    return subjectUserId;
  }

  public AuditEventType eventType() {
    return eventType;
  }

  public AuditPayload payload() {
    return payload;
  }

  public Instant occurredAt() {
    return occurredAt;
  }

  public Instant recordedAt() {
    return recordedAt;
  }

  public long version() {
    return version;
  }
}
