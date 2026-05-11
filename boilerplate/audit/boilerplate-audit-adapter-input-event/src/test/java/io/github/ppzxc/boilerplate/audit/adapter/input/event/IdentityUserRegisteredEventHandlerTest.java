package io.github.ppzxc.boilerplate.audit.adapter.input.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import io.github.ppzxc.boilerplate.audit.application.dto.RecordUserRegisteredAuditCommand;
import io.github.ppzxc.boilerplate.audit.application.port.input.RecordUserRegisteredAuditUseCase;
import io.github.ppzxc.boilerplate.shared.UserRegisteredIntegrationEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ApplicationModuleTest
class IdentityUserRegisteredEventHandlerTest {

  @MockitoBean RecordUserRegisteredAuditUseCase useCase;

  @Test
  void UserRegisteredIntegrationEvent_수신_시_UseCase_호출(Scenario scenario) throws Exception {
    var captured = new AtomicReference<RecordUserRegisteredAuditCommand>();
    doAnswer(
            inv -> {
              captured.set(inv.getArgument(0));
              return null;
            })
        .when(useCase)
        .execute(any());

    var userId = UUID.randomUUID();
    var occurredAt = Instant.parse("2026-01-01T00:00:00Z");
    var event = new UserRegisteredIntegrationEvent(userId, "홍길동", "test@example.com", occurredAt);

    scenario
        .publish(event)
        .andWaitForStateChange(() -> captured.get())
        .andVerify(
            cmd -> {
              assertThat(cmd.subjectUserId()).isEqualTo(userId.toString());
              assertThat(cmd.userName()).isEqualTo("홍길동");
              assertThat(cmd.email()).isEqualTo("test@example.com");
              assertThat(cmd.occurredAt()).isEqualTo(occurredAt.toString());
            });
  }
}
