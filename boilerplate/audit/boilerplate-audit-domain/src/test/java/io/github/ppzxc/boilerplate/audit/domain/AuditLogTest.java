package io.github.ppzxc.boilerplate.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ppzxc.boilerplate.test.DomainTestBase;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditLogTest extends DomainTestBase {

  private static final UUID SUBJECT_UUID = fixedUuidV7(1_700_000_000_000L);
  private static final AuditedUserId SUBJECT = new AuditedUserId(SUBJECT_UUID);
  private static final AuditPayload PAYLOAD =
      new AuditPayload("{\"userName\":\"홍길동\",\"email\":\"test@example.com\"}");

  @Test
  void create_정상_필드_검증() {
    var log = AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, LATER);

    assertThat(log.id()).isNotNull();
    assertThat(log.subjectUserId()).isEqualTo(SUBJECT);
    assertThat(log.eventType()).isEqualTo(AuditEventType.USER_REGISTERED);
    assertThat(log.payload()).isEqualTo(PAYLOAD);
    assertThat(log.occurredAt()).isEqualTo(NOW);
    assertThat(log.recordedAt()).isEqualTo(LATER);
    assertThat(log.version()).isEqualTo(0L);
  }

  @Test
  void create_append_only_이벤트_없음() {
    var log = AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, LATER);
    assertThat(log.pullDomainEvents()).isEmpty();
  }

  @Test
  void create_null_subjectUserId_실패() {
    assertThatThrownBy(
            () -> AuditLog.create(null, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, LATER))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_null_eventType_실패() {
    assertThatThrownBy(() -> AuditLog.create(SUBJECT, null, PAYLOAD, NOW, LATER))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_null_payload_실패() {
    assertThatThrownBy(
            () -> AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, null, NOW, LATER))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_null_occurredAt_실패() {
    assertThatThrownBy(
            () -> AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, null, LATER))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_null_recordedAt_실패() {
    assertThatThrownBy(
            () -> AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void reconstitute_이벤트_없음() {
    var id = AuditLogId.generate();
    var log =
        AuditLog.reconstitute(id, SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, LATER, 0L);

    assertThat(log.id()).isEqualTo(id);
    assertThat(log.pullDomainEvents()).isEmpty();
  }

  @Test
  void pullDomainEvents_두번_호출_항상_빈목록() {
    var log = AuditLog.create(SUBJECT, AuditEventType.USER_REGISTERED, PAYLOAD, NOW, LATER);
    assertThat(log.pullDomainEvents()).isEmpty();
    assertThat(log.pullDomainEvents()).isEmpty();
  }
}
