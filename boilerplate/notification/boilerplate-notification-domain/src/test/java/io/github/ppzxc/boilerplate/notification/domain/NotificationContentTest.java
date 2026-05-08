package io.github.ppzxc.boilerplate.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ppzxc.boilerplate.test.DomainTestBase;
import org.junit.jupiter.api.Test;

class NotificationContentTest extends DomainTestBase {

  @Test
  void 정상_생성() {
    var content = new NotificationContent("제목", "본문");
    assertThat(content.subject()).isEqualTo("제목");
    assertThat(content.body()).isEqualTo("본문");
  }

  @Test
  void subject_200자_경계_성공() {
    var subject = "a".repeat(200);
    assertThat(new NotificationContent(subject, "본문").subject()).hasSize(200);
  }

  @Test
  void subject_201자_초과_실패() {
    var subject = "a".repeat(201);
    assertThatThrownBy(() -> new NotificationContent(subject, "본문"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("subject");
  }

  @Test
  void body_5000자_경계_성공() {
    var body = "a".repeat(5000);
    assertThat(new NotificationContent("제목", body).body()).hasSize(5000);
  }

  @Test
  void body_5001자_초과_실패() {
    var body = "a".repeat(5001);
    assertThatThrownBy(() -> new NotificationContent("제목", body))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("body");
  }

  @Test
  void subject_null_실패() {
    assertThatThrownBy(() -> new NotificationContent(null, "본문"))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void body_null_실패() {
    assertThatThrownBy(() -> new NotificationContent("제목", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void subject_blank_실패() {
    assertThatThrownBy(() -> new NotificationContent("  ", "본문"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("subject");
  }

  @Test
  void body_blank_실패() {
    assertThatThrownBy(() -> new NotificationContent("제목", "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("body");
  }
}
