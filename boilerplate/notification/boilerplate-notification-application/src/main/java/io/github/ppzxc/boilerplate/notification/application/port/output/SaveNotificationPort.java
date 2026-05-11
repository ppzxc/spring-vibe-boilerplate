package io.github.ppzxc.boilerplate.notification.application.port.output;

import io.github.ppzxc.boilerplate.notification.domain.Notification;

public interface SaveNotificationPort {

  void save(Notification notification);
}
