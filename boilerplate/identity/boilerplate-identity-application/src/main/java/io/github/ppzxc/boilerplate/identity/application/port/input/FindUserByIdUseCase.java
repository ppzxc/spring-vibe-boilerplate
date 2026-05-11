package io.github.ppzxc.boilerplate.identity.application.port.input;

import io.github.ppzxc.boilerplate.identity.application.dto.FindUserByIdQuery;
import io.github.ppzxc.boilerplate.identity.application.dto.UserSummary;

public interface FindUserByIdUseCase {

  UserSummary execute(FindUserByIdQuery query);
}
