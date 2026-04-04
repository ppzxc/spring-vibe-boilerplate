package io.github.ppzxc.boilerplate.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Todo {

  private @Nullable Long id;
  private String title;
  private boolean completed;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static Todo create(String title) {
    if (title == null || title.isBlank()) {
      throw new DomainException(ErrorCode.INVALID_ARGUMENT, "Title must not be blank");
    }
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
    return new Todo(null, title.strip(), false, now, now);
  }

  public static Todo reconstitute(
      Long id, String title, boolean completed, LocalDateTime createdAt, LocalDateTime updatedAt) {
    return new Todo(id, title, completed, createdAt, updatedAt);
  }

  public Todo complete() {
    return new Todo(
        this.id, this.title, true, this.createdAt, LocalDateTime.now(ZoneId.systemDefault()));
  }

  public Todo uncomplete() {
    return new Todo(
        this.id, this.title, false, this.createdAt, LocalDateTime.now(ZoneId.systemDefault()));
  }

  public Todo updateTitle(String newTitle) {
    if (newTitle == null || newTitle.isBlank()) {
      throw new DomainException(ErrorCode.INVALID_ARGUMENT, "Title must not be blank");
    }
    return new Todo(
        this.id,
        newTitle.strip(),
        this.completed,
        this.createdAt,
        LocalDateTime.now(ZoneId.systemDefault()));
  }
}
