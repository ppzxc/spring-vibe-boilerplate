package io.github.ppzxc.boilerplate.identity.domain.model;

import java.util.Objects;

/** Access Token + Refresh Token 세트. */
public record TokenSet(AccessToken accessToken, RefreshToken refreshToken) {
  public TokenSet {
    Objects.requireNonNull(accessToken, "accessToken must not be null");
    Objects.requireNonNull(refreshToken, "refreshToken must not be null");
  }
}
