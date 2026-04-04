package io.github.ppzxc.boilerplate.application.port.input.command;

import io.github.ppzxc.boilerplate.domain.Todo;

public interface CreateTodoUseCase {
  Todo create(String title);
}
