package io.github.ppzxc.boilerplate.notification.application.port.out;

import io.github.ppzxc.boilerplate.notification.domain.Notification;

public interface SaveNotificationPort {

  void save(Notification notification);
}
