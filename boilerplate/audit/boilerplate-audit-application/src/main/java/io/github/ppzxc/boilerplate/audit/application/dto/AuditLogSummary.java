package io.github.ppzxc.boilerplate.audit.application.dto;

import java.time.Instant;
import java.util.UUID;

/** 감사 로그 요약 DTO — Query Port 반환 타입. */
public record AuditLogSummary(
    UUID id,
    UUID subjectUserId,
    String eventType,
    String payload,
    Instant occurredAt,
    Instant recordedAt) {}
