package io.github.ppzxc.boilerplate.adapter.input.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.boilerplate.domain.Todo;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TodoController.class)
class TodoControllerTest {

  @Autowired MockMvc mockMvc;
  @MockitoBean CreateTodoUseCase createTodoUseCase;
  @MockitoBean UpdateTodoUseCase updateTodoUseCase;
  @MockitoBean DeleteTodoUseCase deleteTodoUseCase;
  @MockitoBean FindTodoQuery findTodoQuery;

  static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

  @Test
  void post_todos_returns_201_with_location() throws Exception {
    Todo saved = Todo.reconstitute(1L, "Buy milk", false, NOW, NOW);
    when(createTodoUseCase.create(anyString())).thenReturn(saved);

    mockMvc
        .perform(
            post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Buy milk\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/todos/1"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Buy milk"))
        .andExpect(jsonPath("$.completed").value(false));
  }

  @Test
  void get_todos_id_returns_200() throws Exception {
    Todo todo = Todo.reconstitute(1L, "Buy milk", false, NOW, NOW);
    when(findTodoQuery.findById(1L)).thenReturn(todo);

    mockMvc
        .perform(get("/todos/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Buy milk"));
  }

  @Test
  void get_todos_returns_200_with_total_count() throws Exception {
    when(findTodoQuery.findAll())
        .thenReturn(
            List.of(
                Todo.reconstitute(1L, "A", false, NOW, NOW),
                Todo.reconstitute(2L, "B", true, NOW, NOW)));

    mockMvc
        .perform(get("/todos"))
        .andExpect(status().isOk())
        .andExpect(header().string("Total-Count", "2"))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2));
  }

  @Test
  void get_todos_empty_returns_200_with_zero_count() throws Exception {
    when(findTodoQuery.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/todos"))
        .andExpect(status().isOk())
        .andExpect(header().string("Total-Count", "0"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void patch_todos_id_returns_200() throws Exception {
    Todo updated = Todo.reconstitute(1L, "Buy eggs", true, NOW, NOW);
    when(updateTodoUseCase.update(1L, "Buy eggs", true)).thenReturn(updated);

    mockMvc
        .perform(
            patch("/todos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Buy eggs\", \"completed\": true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Buy eggs"))
        .andExpect(jsonPath("$.completed").value(true));
  }

  @Test
  void delete_todos_id_returns_204() throws Exception {
    mockMvc.perform(delete("/todos/1")).andExpect(status().isNoContent());
  }
}
