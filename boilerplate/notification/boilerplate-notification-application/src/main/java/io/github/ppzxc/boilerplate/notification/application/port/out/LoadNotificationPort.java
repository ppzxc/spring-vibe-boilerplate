package io.github.ppzxc.boilerplate.notification.application.port.out;

import io.github.ppzxc.boilerplate.notification.domain.Notification;
import io.github.ppzxc.boilerplate.notification.domain.NotificationId;
import java.util.Optional;

public interface LoadNotificationPort {

  Optional<Notification> findById(NotificationId id);
}
