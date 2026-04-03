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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** adapter-input-api 모듈의 Bean을 등록하는 AutoConfiguration. */
@AutoConfiguration
public class ApiAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
  }

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
