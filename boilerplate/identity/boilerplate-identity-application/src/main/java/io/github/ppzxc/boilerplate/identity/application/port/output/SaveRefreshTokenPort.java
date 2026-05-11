package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.domain.model.RefreshToken;
import io.github.ppzxc.boilerplate.identity.domain.model.UserId;

/** Refresh Token Save Port — Output Port. */
public interface SaveRefreshTokenPort {
  void save(UserId userId, RefreshToken refreshToken);
}
