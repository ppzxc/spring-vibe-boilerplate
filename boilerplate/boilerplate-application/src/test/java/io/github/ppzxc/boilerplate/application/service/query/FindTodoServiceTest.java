package io.github.ppzxc.boilerplate.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.application.port.output.query.FindTodoPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.Todo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindTodoServiceTest {

  @Mock FindTodoPort findTodoPort;
  @InjectMocks FindTodoService findTodoService;

  @Test
  void findById_returns_todo() {
    LocalDateTime now = LocalDateTime.now();
    Todo todo = Todo.reconstitute(1L, "Buy milk", false, now, now);
    when(findTodoPort.findById(1L)).thenReturn(Optional.of(todo));

    Todo result = findTodoService.findById(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void findById_throws_when_not_found() {
    when(findTodoPort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> findTodoService.findById(99L)).isInstanceOf(DomainException.class);
  }

  @Test
  void findAll_returns_list() {
    LocalDateTime now = LocalDateTime.now();
    when(findTodoPort.findAll()).thenReturn(List.of(Todo.reconstitute(1L, "A", false, now, now)));

    List<Todo> result = findTodoService.findAll();

    assertThat(result).hasSize(1);
  }
}
