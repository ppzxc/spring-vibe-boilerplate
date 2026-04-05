package io.github.ppzxc.boilerplate.autoconfigure.adapter.input.api;

import io.github.ppzxc.boilerplate.adapter.input.api.GlobalExceptionHandler;
import io.github.ppzxc.boilerplate.adapter.input.api.TagController;
import io.github.ppzxc.boilerplate.adapter.input.api.TodoController;
import io.github.ppzxc.boilerplate.application.port.input.command.CreateTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTagQuery;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoSummariesQuery;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** adapter-input-api 모듈의 Bean을 등록하는 AutoConfiguration. */
@AutoConfiguration
public class ApiAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .headers(
            headers ->
                headers
                    .contentTypeOptions(contentTypeOptions -> {})
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .httpStrictTransportSecurity(
                        hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  CorsConfigurationSource corsConfigurationSource(Environment env) {
    String allowedOriginsEnv = env.getProperty("cors.allowed-origins", "*");
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins =
        allowedOriginsEnv.equals("*") ? List.of("*") : Arrays.asList(allowedOriginsEnv.split(","));
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Location", "Total-Count"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
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
      FindTodoQuery findTodoQuery,
      FindTodoSummariesQuery findTodoSummariesQuery) {
    return new TodoController(
        createTodoUseCase,
        updateTodoUseCase,
        deleteTodoUseCase,
        findTodoQuery,
        findTodoSummariesQuery);
  }

  @Bean
  @ConditionalOnMissingBean
  TagController tagController(
      CreateTagUseCase createTagUseCase,
      DeleteTagUseCase deleteTagUseCase,
      FindTagQuery findTagQuery) {
    return new TagController(createTagUseCase, deleteTagUseCase, findTagQuery);
  }
}
