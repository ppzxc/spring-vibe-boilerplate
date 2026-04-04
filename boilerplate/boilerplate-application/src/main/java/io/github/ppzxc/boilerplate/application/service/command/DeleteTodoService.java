package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTodoPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteTodoService implements DeleteTodoUseCase {

  private final DeleteTodoPort deleteTodoPort;

  @Override
  public void delete(long id) {
    deleteTodoPort.deleteById(id);
  }
}
