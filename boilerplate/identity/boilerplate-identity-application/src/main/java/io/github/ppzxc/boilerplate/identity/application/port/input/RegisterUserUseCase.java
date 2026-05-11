package io.github.ppzxc.boilerplate.identity.application.port.input;

import io.github.ppzxc.boilerplate.identity.application.dto.RegisterUserCommand;
import io.github.ppzxc.boilerplate.identity.application.dto.RegisterUserResult;

public interface RegisterUserUseCase {

  RegisterUserResult execute(RegisterUserCommand command);
}
