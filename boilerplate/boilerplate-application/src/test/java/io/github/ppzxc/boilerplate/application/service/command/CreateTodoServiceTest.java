package io.github.ppzxc.boilerplate.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.domain.Todo;
import io.github.ppzxc.boilerplate.domain.TodoFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTodoServiceTest {

  @Mock SaveTodoPort saveTodoPort;
  @InjectMocks CreateTodoService service;

  @Test
  void create_delegates_to_save_port() {
    Todo expected = TodoFixtures.savedTodo(1L, "Buy milk", false);
    when(saveTodoPort.save(any(Todo.class))).thenReturn(expected);

    Todo result = service.create("Buy milk");

    assertThat(result).isEqualTo(expected);
  }
}
