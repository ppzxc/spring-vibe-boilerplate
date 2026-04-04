package io.github.ppzxc.boilerplate.application.port.input.command;

import io.github.ppzxc.boilerplate.domain.Todo;
import org.jspecify.annotations.Nullable;

public interface UpdateTodoUseCase {
  Todo update(long id, @Nullable String title, @Nullable Boolean completed);
}
