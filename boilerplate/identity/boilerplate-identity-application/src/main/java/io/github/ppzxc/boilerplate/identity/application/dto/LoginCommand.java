package io.github.ppzxc.boilerplate.identity.application.dto;

import java.util.Objects;

/** Login Command. */
public record LoginCommand(String email, String password) {
  public LoginCommand {
    Objects.requireNonNull(email, "email must not be null");
    Objects.requireNonNull(password, "password must not be null");
  }
}
