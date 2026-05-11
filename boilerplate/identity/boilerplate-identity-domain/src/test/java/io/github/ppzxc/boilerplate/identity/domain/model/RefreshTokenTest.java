package io.github.ppzxc.boilerplate.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private static final Instant EXPIRES_AT = Instant.parse("2026-01-08T00:00:00Z");
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void 유효한_RefreshToken_생성() {
    var token = new RefreshToken("opaque-token", EXPIRES_AT);
    assertThat(token.value()).isEqualTo("opaque-token");
    assertThat(token.expiresAt()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void null_value_예외() {
    assertThatThrownBy(() -> new RefreshToken(null, EXPIRES_AT))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void null_expiresAt_예외() {
    assertThatThrownBy(() -> new RefreshToken("token", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void 빈_value_예외() {
    assertThatThrownBy(() -> new RefreshToken("  ", EXPIRES_AT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void isExpired_만료되지_않음() {
    var token = new RefreshToken("token", EXPIRES_AT);
    assertThat(token.isExpired(NOW)).isFalse();
  }

  @Test
  void isExpired_만료됨() {
    var token = new RefreshToken("token", NOW);
    assertThat(token.isExpired(EXPIRES_AT)).isTrue();
  }

  @Test
  void 동등성_검증() {
    assertThat(new RefreshToken("token", EXPIRES_AT))
        .isEqualTo(new RefreshToken("token", EXPIRES_AT));
    assertThat(new RefreshToken("token", EXPIRES_AT))
        .isNotEqualTo(new RefreshToken("other", EXPIRES_AT));
  }
}
