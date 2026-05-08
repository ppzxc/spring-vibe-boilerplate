package io.github.ppzxc.boilerplate.notification.domain;

import java.util.Objects;

public record NotificationContent(String subject, String body) {

  public NotificationContent {
    Objects.requireNonNull(subject, "subject must not be null");
    Objects.requireNonNull(body, "body must not be null");
    if (subject.isBlank() || subject.length() > 200) {
      throw new IllegalArgumentException("subject must be 1~200 chars: " + subject.length());
    }
    if (body.isBlank() || body.length() > 5000) {
      throw new IllegalArgumentException("body must be 1~5000 chars: " + body.length());
    }
  }
}
