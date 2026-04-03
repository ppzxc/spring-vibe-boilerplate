package io.github.ppzxc.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties =
        "spring.autoconfigure.exclude="
            + "io.github.springwolf.core.configuration.SpringwolfAutoConfiguration,"
            + "io.github.springwolf.plugins.stomp.configuration.SpringwolfStompAutoConfiguration,"
            + "io.github.springwolf.bindings.stomp.configuration.SpringwolfStompBindingAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@Sql(statements = "DELETE FROM todo", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TodoCrudIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    registry.add("spring.jooq.sql-dialect", () -> "POSTGRES");
    registry.add("spring.sql.init.mode", () -> "always");
  }

  @Autowired MockMvc mockMvc;

  @Test
  void create_todo() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"E2E Test Todo\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("E2E Test Todo"))
            .andExpect(jsonPath("$.completed").value(false))
            .andReturn();

    String location = result.getResponse().getHeader("Location");
    assertThat(location).startsWith("/todos/");
  }

  @Test
  void get_todo_by_id() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Get By ID Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    mockMvc
        .perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Get By ID Test"));
  }

  @Test
  void get_all_todos() throws Exception {
    mockMvc
        .perform(
            post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"List Test\"}"))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/todos"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Total-Count"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isNotEmpty());
  }

  @Test
  void update_todo() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Update Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    mockMvc
        .perform(
            patch(location)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Title\", \"completed\": true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"))
        .andExpect(jsonPath("$.completed").value(true));
  }

  @Test
  void delete_todo() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Delete Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    mockMvc.perform(delete(location)).andExpect(status().isNoContent());

    mockMvc.perform(get(location)).andExpect(status().isNotFound());
  }

  @Test
  void get_nonexistent_todo_returns_404() throws Exception {
    mockMvc
        .perform(get("/todos/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
  }
}
