package io.github.ppzxc.boilerplate.identity.application.port.output;

import io.github.ppzxc.boilerplate.identity.domain.model.HashedPassword;

/** Password Encoder Port — Output Port. */
public interface PasswordEncoderPort {
  boolean matches(String rawPassword, HashedPassword encodedPassword);
}
