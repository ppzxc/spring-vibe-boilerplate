package io.github.ppzxc.boilerplate.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.shared.PublishEventPort;
import io.github.ppzxc.boilerplate.domain.Todo;
import io.github.ppzxc.boilerplate.domain.TodoCreatedEvent;
import io.github.ppzxc.boilerplate.domain.TodoFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTodoServiceTest {

  @Mock SaveTodoPort saveTodoPort;
  @Mock PublishEventPort publishEventPort;
  @InjectMocks CreateTodoService service;

  @Test
  void create_delegates_to_save_port() {
    Todo expected = TodoFixtures.savedTodo(1L, "Buy milk", false);
    when(saveTodoPort.save(any(Todo.class))).thenReturn(expected);

    Todo result = service.create("Buy milk");

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void create_delegates_todo_with_correct_title_to_port() {
    when(saveTodoPort.save(any(Todo.class)))
        .thenReturn(TodoFixtures.savedTodo(1L, "Walk the dog", false));

    service.create("Walk the dog");

    ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
    verify(saveTodoPort).save(captor.capture());
    assertThat(captor.getValue().getTitle()).isEqualTo("Walk the dog");
    assertThat(captor.getValue().isCompleted()).isFalse();
  }

  @Test
  void create_publishes_todo_created_event() {
    Todo saved = TodoFixtures.savedTodo(1L, "Buy milk", false);
    when(saveTodoPort.save(any(Todo.class))).thenReturn(saved);

    service.create("Buy milk");

    verify(publishEventPort).publish(new TodoCreatedEvent(1L, "Buy milk"));
  }
}
