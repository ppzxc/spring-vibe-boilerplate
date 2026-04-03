package io.github.ppzxc.template.application.service.command;

import static org.mockito.Mockito.verify;

import io.github.ppzxc.template.application.port.output.command.DeleteTodoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteTodoServiceTest {

  @Mock DeleteTodoPort deleteTodoPort;
  @InjectMocks DeleteTodoService deleteTodoService;

  @Test
  void delete_delegates_to_port() {
    deleteTodoService.delete(1L);
    verify(deleteTodoPort).deleteById(1L);
  }
}
