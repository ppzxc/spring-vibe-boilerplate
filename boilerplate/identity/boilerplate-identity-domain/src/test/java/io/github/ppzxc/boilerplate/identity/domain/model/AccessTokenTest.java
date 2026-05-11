package io.github.ppzxc.boilerplate.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccessTokenTest {

  private static final Instant EXPIRES_AT = Instant.parse("2026-01-01T00:15:00Z");

  @Test
  void 유효한_AccessToken_생성() {
    var token = new AccessToken("jwt-value", EXPIRES_AT);
    assertThat(token.value()).isEqualTo("jwt-value");
    assertThat(token.expiresAt()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void null_value_예외() {
    assertThatThrownBy(() -> new AccessToken(null, EXPIRES_AT))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void 빈_value_예외() {
    assertThatThrownBy(() -> new AccessToken("  ", EXPIRES_AT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void null_expiresAt_예외() {
    assertThatThrownBy(() -> new AccessToken("jwt-value", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void 동등성_검증() {
    assertThat(new AccessToken("jwt-value", EXPIRES_AT))
        .isEqualTo(new AccessToken("jwt-value", EXPIRES_AT));
    assertThat(new AccessToken("jwt-value", EXPIRES_AT))
        .isNotEqualTo(new AccessToken("other", EXPIRES_AT));
  }
}
