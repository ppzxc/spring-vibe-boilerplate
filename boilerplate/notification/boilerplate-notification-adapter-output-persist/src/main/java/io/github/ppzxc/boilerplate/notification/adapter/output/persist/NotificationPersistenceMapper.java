package io.github.ppzxc.boilerplate.notification.adapter.output.persist;

import io.github.ppzxc.boilerplate.notification.domain.Notification;
import io.github.ppzxc.boilerplate.notification.domain.NotificationChannel;
import io.github.ppzxc.boilerplate.notification.domain.NotificationContent;
import io.github.ppzxc.boilerplate.notification.domain.NotificationId;
import io.github.ppzxc.boilerplate.notification.domain.NotificationStatus;
import io.github.ppzxc.boilerplate.notification.domain.RecipientUserId;
import io.github.ppzxc.boilerplate.notification.persistence.jooq.tables.records.NotificationsRecord;

/** Domain ↔ jOOQ Record 변환 매퍼 (AD-4). */
public final class NotificationPersistenceMapper {

  private NotificationPersistenceMapper() {}

  public static Notification toDomain(NotificationsRecord record) {
    return Notification.reconstitute(
        new NotificationId(record.getId()),
        new RecipientUserId(record.getRecipientId()),
        NotificationChannel.valueOf(record.getChannel()),
        NotificationStatus.valueOf(record.getStatus()),
        new NotificationContent(record.getSubject(), record.getBody()),
        record.getVersion());
  }
}
