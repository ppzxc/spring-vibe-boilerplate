package io.github.ppzxc.boilerplate.notification.application.dto;

public record SendUserRegisteredNotificationCommand(
    String recipientUserId, String userName, String email, String occurredAt) {

  public SendUserRegisteredNotificationCommand {
    if (recipientUserId == null || recipientUserId.isBlank()) {
      throw new IllegalArgumentException("recipientUserId must not be blank");
    }
    if (userName == null || userName.isBlank()) {
      throw new IllegalArgumentException("userName must not be blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email must not be blank");
    }
    if (occurredAt == null || occurredAt.isBlank()) {
      throw new IllegalArgumentException("occurredAt must not be blank");
    }
  }
}
