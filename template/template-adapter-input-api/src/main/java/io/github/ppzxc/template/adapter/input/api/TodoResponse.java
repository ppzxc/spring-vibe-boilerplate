package io.github.ppzxc.template.adapter.input.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.ppzxc.template.domain.Todo;
import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TodoResponse(
    @Nullable Long id,
    String title,
    boolean completed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static TodoResponse from(Todo todo) {
    return new TodoResponse(
        todo.getId(),
        todo.getTitle(),
        todo.isCompleted(),
        todo.getCreatedAt(),
        todo.getUpdatedAt());
  }
}
