package io.github.ppzxc.boilerplate.application.port.output.command;

import io.github.ppzxc.boilerplate.domain.Todo;

public interface SaveTodoPort {
  Todo save(Todo todo);
}
