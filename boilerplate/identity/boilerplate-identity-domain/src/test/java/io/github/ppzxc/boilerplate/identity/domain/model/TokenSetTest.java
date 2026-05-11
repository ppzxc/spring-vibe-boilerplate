package io.github.ppzxc.boilerplate.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TokenSetTest {

  private static final Instant ACCESS_EXPIRES = Instant.parse("2026-01-01T00:15:00Z");
  private static final Instant REFRESH_EXPIRES = Instant.parse("2026-01-08T00:00:00Z");

  private static final AccessToken ACCESS = new AccessToken("access", ACCESS_EXPIRES);
  private static final RefreshToken REFRESH = new RefreshToken("refresh", REFRESH_EXPIRES);

  @Test
  void 유효한_TokenSet_생성() {
    var tokenSet = new TokenSet(ACCESS, REFRESH);
    assertThat(tokenSet.accessToken()).isEqualTo(ACCESS);
    assertThat(tokenSet.refreshToken()).isEqualTo(REFRESH);
  }

  @Test
  void null_accessToken_예외() {
    assertThatThrownBy(() -> new TokenSet(null, REFRESH)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void null_refreshToken_예외() {
    assertThatThrownBy(() -> new TokenSet(ACCESS, null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void 동등성_검증() {
    assertThat(new TokenSet(ACCESS, REFRESH)).isEqualTo(new TokenSet(ACCESS, REFRESH));
  }
}
