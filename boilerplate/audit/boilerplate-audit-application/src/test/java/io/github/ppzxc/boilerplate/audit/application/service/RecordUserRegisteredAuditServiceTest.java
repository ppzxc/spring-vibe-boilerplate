package io.github.ppzxc.boilerplate.audit.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.ppzxc.boilerplate.audit.application.dto.RecordUserRegisteredAuditCommand;
import io.github.ppzxc.boilerplate.audit.application.port.out.SaveAuditLogPort;
import io.github.ppzxc.boilerplate.audit.domain.AuditEventType;
import io.github.ppzxc.boilerplate.audit.domain.AuditLog;
import io.github.ppzxc.boilerplate.test.DomainTestBase;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RecordUserRegisteredAuditServiceTest extends DomainTestBase {

  private final SaveAuditLogPort savePort = mock(SaveAuditLogPort.class);
  private final Clock fixedClock = Clock.fixed(LATER, ZoneOffset.UTC);
  private final RecordUserRegisteredAuditService sut =
      new RecordUserRegisteredAuditService(savePort, fixedClock);

  private static final String USER_ID = UUID.randomUUID().toString();
  private static final String OCCURRED_AT = NOW.toString();

  @Test
  void 정상_감사로그_저장() {
    var command =
        new RecordUserRegisteredAuditCommand(USER_ID, "홍길동", "test@example.com", OCCURRED_AT);

    sut.execute(command);

    var captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(savePort).save(captor.capture());
    var saved = captor.getValue();
    assertThat(saved.subjectUserId().value()).isEqualTo(UUID.fromString(USER_ID));
    assertThat(saved.eventType()).isEqualTo(AuditEventType.USER_REGISTERED);
    assertThat(saved.occurredAt()).isEqualTo(NOW);
    assertThat(saved.recordedAt()).isEqualTo(LATER);
    assertThat(saved.payload().value()).contains("홍길동").contains("test@example.com");
  }

  @Test
  void payload_JSON_형식_검증() {
    var command =
        new RecordUserRegisteredAuditCommand(USER_ID, "홍길동", "test@example.com", OCCURRED_AT);

    sut.execute(command);

    var captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(savePort).save(captor.capture());
    var payload = captor.getValue().payload().value();
    assertThat(payload).isEqualTo("{\"userName\":\"홍길동\",\"email\":\"test@example.com\"}");
  }

  @Test
  void null_savePort_실패() {
    assertThatThrownBy(() -> new RecordUserRegisteredAuditService(null, fixedClock))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void null_clock_실패() {
    assertThatThrownBy(() -> new RecordUserRegisteredAuditService(savePort, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void JSON_escape_캐리지리턴() {
    assertThat(RecordUserRegisteredAuditService.jsonEscape("a\rb")).isEqualTo("a\\rb");
  }

  @Test
  void JSON_escape_탭() {
    assertThat(RecordUserRegisteredAuditService.jsonEscape("a\tb")).isEqualTo("a\\tb");
  }

  @Test
  void JSON_escape_따옴표() {
    assertThat(RecordUserRegisteredAuditService.jsonEscape("Bob \"Builder\""))
        .isEqualTo("Bob \\\"Builder\\\"");
  }

  @Test
  void JSON_escape_역슬래시() {
    assertThat(RecordUserRegisteredAuditService.jsonEscape("C:\\path")).isEqualTo("C:\\\\path");
  }

  @Test
  void JSON_escape_줄바꿈() {
    assertThat(RecordUserRegisteredAuditService.jsonEscape("line1\nline2"))
        .isEqualTo("line1\\nline2");
  }

  @Test
  void 잘못된_UUID_형식_예외() {
    var command = new RecordUserRegisteredAuditCommand("not-a-uuid", "홍길동", "a@b.com", OCCURRED_AT);
    assertThatThrownBy(() -> sut.execute(command)).isInstanceOf(IllegalArgumentException.class);
    verify(savePort, never()).save(any());
  }
}
