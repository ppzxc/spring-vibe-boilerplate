package io.github.ppzxc.boilerplate.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HashedPasswordTest {

  @Test
  void 유효한_HashedPassword_생성() {
    var pw = new HashedPassword("$2a$12$hashed");
    assertThat(pw.value()).isEqualTo("$2a$12$hashed");
  }

  @Test
  void null_예외() {
    assertThatThrownBy(() -> new HashedPassword(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void 빈_값_예외() {
    assertThatThrownBy(() -> new HashedPassword("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 동등성_검증() {
    assertThat(new HashedPassword("hash")).isEqualTo(new HashedPassword("hash"));
    assertThat(new HashedPassword("hash")).isNotEqualTo(new HashedPassword("other"));
  }
}
