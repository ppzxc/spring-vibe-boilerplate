package io.github.ppzxc.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties =
        "spring.autoconfigure.exclude="
            + "io.github.springwolf.core.configuration.SpringwolfAutoConfiguration,"
            + "io.github.springwolf.plugins.stomp.configuration.SpringwolfStompAutoConfiguration,"
            + "io.github.springwolf.bindings.stomp.configuration.SpringwolfStompBindingAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoCrudIT {

  @Autowired MockMvc mockMvc;

  @Test
  @Order(1)
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
  @Order(2)
  void get_todo_by_id() throws Exception {
    // Create first
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Get By ID Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    // Get
    mockMvc
        .perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Get By ID Test"));
  }

  @Test
  @Order(3)
  void get_all_todos() throws Exception {
    mockMvc
        .perform(get("/todos"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Total-Count"))
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @Order(4)
  void update_todo() throws Exception {
    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Update Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    // Update
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
  @Order(5)
  void delete_todo() throws Exception {
    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Delete Test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");

    // Delete
    mockMvc.perform(delete(location)).andExpect(status().isNoContent());

    // Verify not found
    mockMvc.perform(get(location)).andExpect(status().isNotFound());
  }

  @Test
  @Order(6)
  void get_nonexistent_todo_returns_404() throws Exception {
    mockMvc
        .perform(get("/todos/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
  }
}
