package io.github.ppzxc.template.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ppzxc.template.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.template.domain.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTodoServiceTest {

  @Mock SaveTodoPort saveTodoPort;
  @InjectMocks CreateTodoService createTodoService;

  @Test
  void create_saves_and_returns_todo() {
    Todo saved =
        Todo.reconstitute(
            1L, "Buy milk", false, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    when(saveTodoPort.save(any(Todo.class))).thenReturn(saved);

    Todo result = createTodoService.create("Buy milk");

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("Buy milk");
    verify(saveTodoPort).save(any(Todo.class));
  }
}
