package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTodoPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import io.github.ppzxc.boilerplate.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class UpdateTodoService implements UpdateTodoUseCase {

  private final FindTodoPort findTodoPort;
  private final SaveTodoPort saveTodoPort;

  @Override
  public Todo update(long id, @Nullable String title, @Nullable Boolean completed) {
    Todo todo =
        findTodoPort
            .findById(id)
            .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Todo not found: " + id));

    if (title != null) {
      todo = todo.updateTitle(title);
    }
    if (completed != null) {
      todo = completed ? todo.complete() : todo.uncomplete();
    }
    return saveTodoPort.save(todo);
  }
}
