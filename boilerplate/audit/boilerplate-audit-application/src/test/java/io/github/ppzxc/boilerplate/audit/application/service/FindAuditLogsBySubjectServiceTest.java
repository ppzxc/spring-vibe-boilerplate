package io.github.ppzxc.boilerplate.audit.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.FindAuditLogsBySubjectQuery;
import io.github.ppzxc.boilerplate.audit.application.port.out.AuditLogQueryPort;
import io.github.ppzxc.boilerplate.test.DomainTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FindAuditLogsBySubjectServiceTest extends DomainTestBase {

  private final AuditLogQueryPort queryPort = mock(AuditLogQueryPort.class);
  private final FindAuditLogsBySubjectService sut = new FindAuditLogsBySubjectService(queryPort);

  private static final String USER_ID = UUID.randomUUID().toString();

  @Test
  void 정상_조회() {
    var summary =
        new AuditLogSummary(
            UUID.randomUUID(), UUID.fromString(USER_ID), "USER_REGISTERED", "{}", NOW, LATER);
    when(queryPort.findBySubjectUserId(USER_ID, 10)).thenReturn(List.of(summary));

    var result = sut.execute(new FindAuditLogsBySubjectQuery(USER_ID, 10));

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(summary);
    verify(queryPort).findBySubjectUserId(USER_ID, 10);
  }

  @Test
  void 결과_없음() {
    when(queryPort.findBySubjectUserId(USER_ID, 10)).thenReturn(List.of());
    var result = sut.execute(new FindAuditLogsBySubjectQuery(USER_ID, 10));
    assertThat(result).isEmpty();
  }

  @Test
  void null_queryPort_실패() {
    assertThatThrownBy(() -> new FindAuditLogsBySubjectService(null))
        .isInstanceOf(NullPointerException.class);
  }
}
