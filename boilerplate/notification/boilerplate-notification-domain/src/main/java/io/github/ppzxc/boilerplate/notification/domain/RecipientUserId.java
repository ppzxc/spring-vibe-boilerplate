package io.github.ppzxc.boilerplate.notification.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity UserId의 ACL 경계 표현 — Notification BC가 Identity 도메인에 직접 의존하지 않음. */
public record RecipientUserId(UUID value) {

  public RecipientUserId {
    Objects.requireNonNull(value, "RecipientUserId must not be null");
  }
}
