package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.domain.model.User;

public interface SaveUserPort {

  User save(User user);
}
