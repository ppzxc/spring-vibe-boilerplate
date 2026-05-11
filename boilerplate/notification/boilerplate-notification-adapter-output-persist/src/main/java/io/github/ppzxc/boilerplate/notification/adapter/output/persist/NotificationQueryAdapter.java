package io.github.ppzxc.boilerplate.notification.adapter.output.persist;

import static io.github.ppzxc.boilerplate.notification.persistence.jooq.Tables.NOTIFICATIONS;

import io.github.ppzxc.boilerplate.notification.application.dto.NotificationSummary;
import io.github.ppzxc.boilerplate.notification.application.port.output.NotificationQueryPort;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueryAdapter implements NotificationQueryPort {

  private final DSLContext dsl;

  public NotificationQueryAdapter(DSLContext dsl) {
    this.dsl = Objects.requireNonNull(dsl, "dsl must not be null");
  }

  @Override
  public Optional<NotificationSummary> findSummaryById(String notificationId) {
    return dsl.selectFrom(NOTIFICATIONS)
        .where(NOTIFICATIONS.ID.eq(UUID.fromString(notificationId)))
        .fetchOptional(
            r -> new NotificationSummary(r.getId().toString(), r.getStatus(), r.getChannel()));
  }
}
