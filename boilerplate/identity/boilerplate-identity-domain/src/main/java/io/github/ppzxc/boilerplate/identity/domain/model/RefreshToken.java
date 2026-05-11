package io.github.ppzxc.boilerplate.identity.domain.model;

import java.time.Instant;
import java.util.Objects;

/** Refresh Token Value Object — DB 저장용 정보를 포함할 수 있음. */
public record RefreshToken(String value, Instant expiresAt) {
  public RefreshToken {
    Objects.requireNonNull(value, "value must not be null");
    Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
  }

  public boolean isExpired(Instant now) {
    return expiresAt.isBefore(now);
  }
}
