package io.github.ppzxc.template.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.ppzxc.template.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.template.application.port.output.query.FindTodoPort;
import io.github.ppzxc.template.domain.DomainException;
import io.github.ppzxc.template.domain.ErrorCode;
import io.github.ppzxc.template.domain.Todo;
import io.github.ppzxc.template.domain.TodoFixtures;
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
  @InjectMocks UpdateTodoService service;

  @Test
  void update_title_only() {
    Todo original = TodoFixtures.savedTodo(1L, "original", false);
    when(findTodoPort.findById(1L)).thenReturn(Optional.of(original));
    when(saveTodoPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Todo result = service.update(1L, "updated", null);

    assertThat(result.getTitle()).isEqualTo("updated");
    assertThat(result.isCompleted()).isFalse();
  }

  @Test
  void update_completed_only() {
    Todo original = TodoFixtures.savedTodo(1L, "original", false);
    when(findTodoPort.findById(1L)).thenReturn(Optional.of(original));
    when(saveTodoPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Todo result = service.update(1L, null, true);

    assertThat(result.getTitle()).isEqualTo("original");
    assertThat(result.isCompleted()).isTrue();
  }

  @Test
  void update_throws_when_not_found() {
    when(findTodoPort.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(999L, "x", null))
        .isInstanceOf(DomainException.class)
        .extracting(e -> ((DomainException) e).errorCode())
        .isEqualTo(ErrorCode.NOT_FOUND);
  }
}
