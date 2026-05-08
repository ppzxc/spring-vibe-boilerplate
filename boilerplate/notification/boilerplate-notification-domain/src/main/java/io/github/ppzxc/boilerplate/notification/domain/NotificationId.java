package io.github.ppzxc.boilerplate.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {

  public NotificationId {
    Objects.requireNonNull(value, "NotificationId must not be null");
  }

  public static NotificationId generate() {
    return new NotificationId(UUIDv7.generate());
  }
}
