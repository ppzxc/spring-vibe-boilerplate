package io.github.ppzxc.boilerplate.identity.adapter.output.security;

import io.github.ppzxc.boilerplate.identity.application.port.output.PasswordEncoderPort;
import io.github.ppzxc.boilerplate.identity.domain.model.HashedPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public boolean matches(String rawPassword, HashedPassword encodedPassword) {
    return encoder.matches(rawPassword, encodedPassword.value());
  }
}
