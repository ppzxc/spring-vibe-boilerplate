package io.github.ppzxc.boilerplate.identity.application.port.input;

import io.github.ppzxc.boilerplate.identity.application.dto.FindUserByIdQuery;
import io.github.ppzxc.boilerplate.identity.application.dto.UserSummary;
import java.util.Optional;

public interface FindUserByIdUseCase {

  Optional<UserSummary> execute(FindUserByIdQuery query);
}
