package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.shared.PublishEventPort;
import io.github.ppzxc.boilerplate.domain.Todo;
import io.github.ppzxc.boilerplate.domain.TodoCreatedEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTodoService implements CreateTodoUseCase {

  private final SaveTodoPort saveTodoPort;
  private final PublishEventPort publishEventPort;

  @Override
  public Todo create(String title) {
    Todo todo = Todo.create(title);
    Todo saved = saveTodoPort.save(todo);
    Long savedId = saved.getId();
    if (savedId == null) {
      throw new IllegalStateException("Saved todo must have an id");
    }
    publishEventPort.publish(new TodoCreatedEvent(savedId, saved.getTitle()));
    return saved;
  }
}
