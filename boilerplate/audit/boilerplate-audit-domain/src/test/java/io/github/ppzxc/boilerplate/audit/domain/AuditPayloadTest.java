package io.github.ppzxc.boilerplate.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ppzxc.boilerplate.test.DomainTestBase;
import org.junit.jupiter.api.Test;

class AuditPayloadTest extends DomainTestBase {

  @Test
  void 정상_생성() {
    var payload = new AuditPayload("{\"key\":\"value\"}");
    assertThat(payload.value()).isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  void null_실패() {
    assertThatThrownBy(() -> new AuditPayload(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void 빈문자열_실패() {
    assertThatThrownBy(() -> new AuditPayload("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 공백만_실패() {
    assertThatThrownBy(() -> new AuditPayload("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 최대_길이_초과_실패() {
    var tooLong = "a".repeat(10001);
    assertThatThrownBy(() -> new AuditPayload(tooLong))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 최대_길이_경계_정상() {
    var exactly10000 = "a".repeat(10000);
    var payload = new AuditPayload(exactly10000);
    assertThat(payload.value()).hasSize(10000);
  }

  @Test
  void 동등성_검증() {
    var json = "{\"test\":\"value\"}";
    assertThat(new AuditPayload(json)).isEqualTo(new AuditPayload(json));
  }
}
