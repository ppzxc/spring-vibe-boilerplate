package io.github.ppzxc.template.application.port.input.command;

import io.github.ppzxc.template.domain.Todo;

public interface CreateTodoUseCase {
  Todo create(String title);
}
