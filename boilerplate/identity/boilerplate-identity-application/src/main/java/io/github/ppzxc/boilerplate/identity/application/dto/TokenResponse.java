package io.github.ppzxc.boilerplate.identity.application.dto;

import java.util.Objects;

/** Token Response DTO. */
public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {
  public TokenResponse {
    Objects.requireNonNull(accessToken, "accessToken must not be null");
    Objects.requireNonNull(refreshToken, "refreshToken must not be null");
  }
}
