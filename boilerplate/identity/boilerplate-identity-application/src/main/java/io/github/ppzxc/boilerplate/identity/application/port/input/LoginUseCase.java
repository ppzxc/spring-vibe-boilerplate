package io.github.ppzxc.boilerplate.identity.application.port.input;

import io.github.ppzxc.boilerplate.identity.application.dto.LoginCommand;
import io.github.ppzxc.boilerplate.identity.application.dto.TokenResponse;

/** Login UseCase — Input Port. */
public interface LoginUseCase {
  TokenResponse execute(LoginCommand command);
}
