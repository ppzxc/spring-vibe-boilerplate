package io.github.ppzxc.template.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.ppzxc.template.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.template.application.port.output.query.FindTodoPort;
import io.github.ppzxc.template.domain.DomainException;
import io.github.ppzxc.template.domain.Todo;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTodoServiceTest {

  @Mock FindTodoPort findTodoPort;
  @Mock SaveTodoPort saveTodoPort;
  @InjectMocks UpdateTodoService updateTodoService;

  @Test
  void update_title_only() {
    LocalDateTime now = LocalDateTime.now();
    Todo existing = Todo.reconstitute(1L, "Old title", false, now, now);
    when(findTodoPort.findById(1L)).thenReturn(Optional.of(existing));
    when(saveTodoPort.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

    Todo result = updateTodoService.update(1L, "New title", null);

    assertThat(result.getTitle()).isEqualTo("New title");
    assertThat(result.isCompleted()).isFalse();
  }

  @Test
  void update_completed_only() {
    LocalDateTime now = LocalDateTime.now();
    Todo existing = Todo.reconstitute(1L, "Buy milk", false, now, now);
    when(findTodoPort.findById(1L)).thenReturn(Optional.of(existing));
    when(saveTodoPort.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

    Todo result = updateTodoService.update(1L, null, true);

    assertThat(result.getTitle()).isEqualTo("Buy milk");
    assertThat(result.isCompleted()).isTrue();
  }

  @Test
  void update_throws_when_not_found() {
    when(findTodoPort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> updateTodoService.update(99L, "title", null))
        .isInstanceOf(DomainException.class);
  }
}
