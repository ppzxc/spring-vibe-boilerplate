package io.github.ppzxc.boilerplate.audit.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.audit.application.dto.AuditLogSummary;
import io.github.ppzxc.boilerplate.audit.application.dto.ListRecentAuditLogsQuery;
import io.github.ppzxc.boilerplate.audit.application.port.out.AuditLogQueryPort;
import io.github.ppzxc.boilerplate.test.DomainTestBase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListRecentAuditLogsServiceTest extends DomainTestBase {

  private final AuditLogQueryPort queryPort = mock(AuditLogQueryPort.class);
  private final ListRecentAuditLogsService sut = new ListRecentAuditLogsService(queryPort);

  @Test
  void 정상_조회() {
    var summary =
        new AuditLogSummary(
            UUID.randomUUID(), UUID.randomUUID(), "USER_REGISTERED", "{}", NOW, LATER);
    when(queryPort.findRecent(20)).thenReturn(List.of(summary));

    var result = sut.execute(new ListRecentAuditLogsQuery(20));

    assertThat(result).hasSize(1);
    verify(queryPort).findRecent(20);
  }

  @Test
  void 결과_없음() {
    when(queryPort.findRecent(10)).thenReturn(List.of());
    assertThat(sut.execute(new ListRecentAuditLogsQuery(10))).isEmpty();
  }

  @Test
  void null_queryPort_실패() {
    assertThatThrownBy(() -> new ListRecentAuditLogsService(null))
        .isInstanceOf(NullPointerException.class);
  }
}
