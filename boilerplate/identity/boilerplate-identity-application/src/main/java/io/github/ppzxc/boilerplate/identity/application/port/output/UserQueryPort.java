package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.application.dto.UserSummary;
import java.util.List;
import java.util.Optional;

public interface UserQueryPort {

  Optional<UserSummary> findSummaryById(String userId);

  List<UserSummary> findAll();
}
