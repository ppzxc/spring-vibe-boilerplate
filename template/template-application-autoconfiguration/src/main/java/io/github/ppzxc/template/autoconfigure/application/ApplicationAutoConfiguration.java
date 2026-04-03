package io.github.ppzxc.template.autoconfigure.application;

import io.github.ppzxc.template.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.template.application.port.output.command.DeleteTodoPort;
import io.github.ppzxc.template.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.template.application.port.output.query.FindTodoPort;
import io.github.ppzxc.template.application.service.command.CreateTodoService;
import io.github.ppzxc.template.application.service.command.DeleteTodoService;
import io.github.ppzxc.template.application.service.command.UpdateTodoService;
import io.github.ppzxc.template.application.service.query.FindTodoService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@AutoConfiguration
public class ApplicationAutoConfiguration {

  @Bean
  @Transactional
  CreateTodoUseCase createTodoUseCase(SaveTodoPort saveTodoPort) {
    return new CreateTodoService(saveTodoPort);
  }

  @Bean
  @Transactional
  UpdateTodoUseCase updateTodoUseCase(FindTodoPort findTodoPort, SaveTodoPort saveTodoPort) {
    return new UpdateTodoService(findTodoPort, saveTodoPort);
  }

  @Bean
  @Transactional
  DeleteTodoUseCase deleteTodoUseCase(DeleteTodoPort deleteTodoPort) {
    return new DeleteTodoService(deleteTodoPort);
  }

  @Bean
  @Transactional(readOnly = true)
  FindTodoQuery findTodoQuery(FindTodoPort findTodoPort) {
    return new FindTodoService(findTodoPort);
  }
}
