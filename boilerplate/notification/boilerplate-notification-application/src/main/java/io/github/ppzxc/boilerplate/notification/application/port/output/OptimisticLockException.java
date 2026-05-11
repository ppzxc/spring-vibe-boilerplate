package io.github.ppzxc.boilerplate.notification.application.port.output;

import io.github.ppzxc.boilerplate.notification.domain.NotificationId;
import java.util.Objects;

public class OptimisticLockException extends RuntimeException {

  private final NotificationId notificationId;

  public OptimisticLockException(NotificationId notificationId) {
    super("Optimistic lock conflict for notification: " + notificationId.value());
    this.notificationId = Objects.requireNonNull(notificationId, "notificationId must not be null");
  }

  public NotificationId notificationId() {
    return notificationId;
  }
}
