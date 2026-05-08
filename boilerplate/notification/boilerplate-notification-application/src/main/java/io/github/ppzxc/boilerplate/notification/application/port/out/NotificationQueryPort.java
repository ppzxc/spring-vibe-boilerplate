package io.github.ppzxc.boilerplate.notification.application.port.out;

import io.github.ppzxc.boilerplate.notification.application.dto.NotificationSummary;
import java.util.Optional;

public interface NotificationQueryPort {

  Optional<NotificationSummary> findSummaryById(String notificationId);
}
