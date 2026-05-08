package io.github.ppzxc.boilerplate.shared;

import java.time.Instant;
import java.util.UUID;

/** Identity BC: 사용자 등록 Integration Event (Published Language). */
public record UserRegisteredIntegrationEvent(
    UUID userId, String userName, String email, Instant occurredAt) implements IntegrationEvent {}
