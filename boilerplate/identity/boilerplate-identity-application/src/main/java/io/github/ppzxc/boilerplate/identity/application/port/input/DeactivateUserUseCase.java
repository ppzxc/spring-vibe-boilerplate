package io.github.ppzxc.boilerplate.identity.application.port.input;

import io.github.ppzxc.boilerplate.identity.application.dto.DeactivateUserCommand;

public interface DeactivateUserUseCase {

  void execute(DeactivateUserCommand command);
}
