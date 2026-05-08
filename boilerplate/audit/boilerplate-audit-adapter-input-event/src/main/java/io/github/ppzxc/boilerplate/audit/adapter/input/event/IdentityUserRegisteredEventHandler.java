package io.github.ppzxc.boilerplate.audit.adapter.input.event;

import io.github.ppzxc.boilerplate.audit.application.dto.RecordUserRegisteredAuditCommand;
import io.github.ppzxc.boilerplate.audit.application.port.in.RecordUserRegisteredAuditUseCase;
import io.github.ppzxc.boilerplate.shared.UserRegisteredIntegrationEvent;
import java.util.Objects;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class IdentityUserRegisteredEventHandler {

  private final RecordUserRegisteredAuditUseCase useCase;

  public IdentityUserRegisteredEventHandler(RecordUserRegisteredAuditUseCase useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase must not be null");
  }

  @ApplicationModuleListener
  void on(UserRegisteredIntegrationEvent event) {
    useCase.execute(
        new RecordUserRegisteredAuditCommand(
            event.userId().toString(),
            event.userName(),
            event.email(),
            event.occurredAt().toString()));
  }
}
