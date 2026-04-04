package io.github.ppzxc.template.adapter.input.api;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.ppzxc.template.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.template.domain.Todo;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Spring Cloud Contract 서버 사이드 테스트 베이스 클래스.
 *
 * <p>계약 파일에서 자동 생성된 ContractTest 클래스들이 이 클래스를 extends한다. MockMvc를 RestAssuredMockMvc에 등록하여 계약 검증에
 * 사용한다.
 */
@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TodoController.class, ContractBaseTest.JacksonConfig.class})
public abstract class ContractBaseTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

  @Autowired MockMvc mockMvc;

  @MockitoBean CreateTodoUseCase createTodoUseCase;
  @MockitoBean UpdateTodoUseCase updateTodoUseCase;
  @MockitoBean DeleteTodoUseCase deleteTodoUseCase;
  @MockitoBean FindTodoQuery findTodoQuery;

  @BeforeEach
  void setUp() {
    RestAssuredMockMvc.mockMvc(mockMvc);

    Todo milk = Todo.reconstitute(1L, "Buy milk", false, NOW, NOW);
    Todo eggs = Todo.reconstitute(2L, "Buy eggs", true, NOW, NOW);

    when(createTodoUseCase.create("Buy milk")).thenReturn(milk);
    when(findTodoQuery.findById(1L)).thenReturn(milk);
    when(findTodoQuery.findAll()).thenReturn(List.of(milk, eggs));
  }

  @Configuration
  static class JacksonConfig {

    @Bean
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
      ObjectMapper mapper =
          new ObjectMapper()
              .registerModule(new JavaTimeModule())
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return new MappingJackson2HttpMessageConverter(mapper);
    }
  }
}
