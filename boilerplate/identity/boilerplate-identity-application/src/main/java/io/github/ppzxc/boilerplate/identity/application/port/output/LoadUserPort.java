package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.domain.model.Email;
import io.github.ppzxc.boilerplate.identity.domain.model.User;
import io.github.ppzxc.boilerplate.identity.domain.model.UserId;
import java.util.Optional;

public interface LoadUserPort {

  Optional<User> findById(UserId id);

  Optional<User> findByEmail(Email email);

  boolean existsByEmail(Email email);
}
