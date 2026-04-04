package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.domain.Todo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTodoService implements CreateTodoUseCase {

  private final SaveTodoPort saveTodoPort;

  @Override
  public Todo create(String title) {
    Todo todo = Todo.create(title);
    return saveTodoPort.save(todo);
  }
}
