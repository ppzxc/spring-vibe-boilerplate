package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.domain.model.TokenSet;
import io.github.ppzxc.boilerplate.identity.domain.model.User;

/** JWT Issue Port — Output Port. */
public interface IssueTokenPort {
  TokenSet issue(User user);
}
