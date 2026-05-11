package io.github.ppzxc.boilerplate.identity.domain.model;

import java.util.Objects;

/** JWT Access Token Value Object. */
public record AccessToken(String value) {
  public AccessToken {
    Objects.requireNonNull(value, "value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
  }
}
