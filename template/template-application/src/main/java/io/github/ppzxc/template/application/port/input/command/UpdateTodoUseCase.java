package io.github.ppzxc.template.application.port.input.command;

import io.github.ppzxc.template.domain.Todo;
import org.jspecify.annotations.Nullable;

public interface UpdateTodoUseCase {
  Todo update(long id, @Nullable String title, @Nullable Boolean completed);
}
