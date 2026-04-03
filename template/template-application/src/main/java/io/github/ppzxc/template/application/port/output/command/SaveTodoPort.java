package io.github.ppzxc.template.application.port.output.command;

import io.github.ppzxc.template.domain.Todo;

public interface SaveTodoPort {
  Todo save(Todo todo);
}
