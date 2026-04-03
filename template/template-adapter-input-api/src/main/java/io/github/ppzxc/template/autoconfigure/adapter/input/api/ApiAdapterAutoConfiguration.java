package io.github.ppzxc.template.autoconfigure.adapter.input.api;

import io.github.ppzxc.template.adapter.input.api.GlobalExceptionHandler;
import io.github.ppzxc.template.adapter.input.api.TodoController;
import io.github.ppzxc.template.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** adapter-input-api 모듈의 Bean을 등록하는 AutoConfiguration. */
@AutoConfiguration
public class ApiAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  GlobalExceptionHandler globalExceptionHandler() {
    return new GlobalExceptionHandler();
  }

  @Bean
  @ConditionalOnMissingBean
  TodoController todoController(
      CreateTodoUseCase createTodoUseCase,
      UpdateTodoUseCase updateTodoUseCase,
      DeleteTodoUseCase deleteTodoUseCase,
      FindTodoQuery findTodoQuery) {
    return new TodoController(
        createTodoUseCase, updateTodoUseCase, deleteTodoUseCase, findTodoQuery);
  }
}
