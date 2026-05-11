package io.github.ppzxc.boilerplate.identity.application.service;

import io.github.ppzxc.boilerplate.identity.application.dto.FindUserByIdQuery;
import io.github.ppzxc.boilerplate.identity.application.dto.UserSummary;
import io.github.ppzxc.boilerplate.identity.application.port.input.FindUserByIdUseCase;
import io.github.ppzxc.boilerplate.identity.application.port.output.UserQueryPort;
import java.util.Objects;
import java.util.Optional;

public class FindUserByIdService implements FindUserByIdUseCase {

  private final UserQueryPort queryPort;

  public FindUserByIdService(UserQueryPort queryPort) {
    this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
  }

  @Override
  public Optional<UserSummary> execute(FindUserByIdQuery query) {
    return queryPort.findSummaryById(query.userId());
  }
}
