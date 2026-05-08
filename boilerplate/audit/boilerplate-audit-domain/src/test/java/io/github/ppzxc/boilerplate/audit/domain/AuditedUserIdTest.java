package io.github.ppzxc.boilerplate.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ppzxc.boilerplate.test.DomainTestBase;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditedUserIdTest extends DomainTestBase {

  @Test
  void 정상_생성() {
    var uuid = UUID.randomUUID();
    var id = new AuditedUserId(uuid);
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void null_값_실패() {
    assertThatThrownBy(() -> new AuditedUserId(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void 동등성_검증() {
    var uuid = UUID.randomUUID();
    assertThat(new AuditedUserId(uuid)).isEqualTo(new AuditedUserId(uuid));
  }
}
