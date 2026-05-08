package io.github.ppzxc.boilerplate.notification.application.port.in;

import io.github.ppzxc.boilerplate.notification.application.dto.NotificationSummary;
import io.github.ppzxc.boilerplate.notification.application.dto.SendUserRegisteredNotificationCommand;

public interface SendUserRegisteredNotificationUseCase {

  NotificationSummary execute(SendUserRegisteredNotificationCommand command);
}
