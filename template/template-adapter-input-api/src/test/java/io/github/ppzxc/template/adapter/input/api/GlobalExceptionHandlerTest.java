package io.github.ppzxc.template.adapter.input.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.ppzxc.template.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.template.domain.DomainException;
import io.github.ppzxc.template.domain.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = {GlobalExceptionHandlerTest.TestController.class, TodoController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

  @MockitoBean private CreateTodoUseCase createTodoUseCase;
  @MockitoBean private UpdateTodoUseCase updateTodoUseCase;
  @MockitoBean private DeleteTodoUseCase deleteTodoUseCase;
  @MockitoBean private FindTodoQuery findTodoQuery;

  @Autowired private MockMvc mockMvc;

  @Test
  void domain_exception_returns_problem_detail_with_error_code() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void domain_exception_with_field_violations() throws Exception {
    mockMvc
        .perform(get("/test/validation"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
        .andExpect(jsonPath("$.details[0].field").value("email"))
        .andExpect(jsonPath("$.details[0].description").value("must be a valid email"));
  }

  @Test
  void validation_error_returns_400_with_field_details() throws Exception {
    mockMvc
        .perform(
            post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"\"}")) // blank title — @NotBlank 위반
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
        .andExpect(jsonPath("$.details").isArray())
        .andExpect(jsonPath("$.details[0].field").value("title"));
  }

  @Test
  void unhandled_exception_returns_500_internal() throws Exception {
    mockMvc
        .perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.errorCode").value("INTERNAL"));
  }

  @Test
  void negative_id_returns_400() throws Exception {
    mockMvc
        .perform(get("/todos/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));
  }

  @SpringBootApplication
  static class TestApplication {}

  @RestController
  static class TestController {

    @GetMapping("/test/not-found")
    String notFound() {
      throw new DomainException(ErrorCode.NOT_FOUND, "Order not found");
    }

    @GetMapping("/test/validation")
    String validation() {
      throw DomainException.withDetails(
          ErrorCode.INVALID_ARGUMENT,
          "Validation failed",
          List.of(new DomainException.FieldViolation("email", "must be a valid email")));
    }

    @GetMapping("/test/unexpected")
    String unexpected() {
      throw new RuntimeException("Something went wrong");
    }
  }
}
