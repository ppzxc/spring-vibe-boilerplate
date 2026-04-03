package io.github.ppzxc.template.adapter.output.persist;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ppzxc.template.domain.Todo;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(classes = TodoPersistAdapterIT.TestConfig.class)
@Sql(scripts = "classpath:db/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TodoPersistAdapterIT {

  @ImportAutoConfiguration({DataSourceAutoConfiguration.class, JooqAutoConfiguration.class})
  static class TestConfig {}

  @Autowired DSLContext dsl;
  TodoPersistAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new TodoPersistAdapter(dsl);
    dsl.execute("DELETE FROM todo");
  }

  @Test
  void save_inserts_new_todo() {
    Todo todo = Todo.create("Buy milk");
    Todo saved = adapter.save(todo);
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("Buy milk");
    assertThat(saved.isCompleted()).isFalse();
  }

  @Test
  void save_updates_existing_todo() {
    Todo saved = adapter.save(Todo.create("Buy milk"));
    Todo updated = saved.updateTitle("Buy eggs");
    Todo result = adapter.save(updated);
    assertThat(result.getId()).isEqualTo(saved.getId());
    assertThat(result.getTitle()).isEqualTo("Buy eggs");
  }

  @Test
  void findById_returns_todo() {
    Todo saved = adapter.save(Todo.create("Buy milk"));
    Optional<Todo> found = adapter.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Buy milk");
  }

  @Test
  void findById_returns_empty_when_not_found() {
    Optional<Todo> found = adapter.findById(999L);
    assertThat(found).isEmpty();
  }

  @Test
  void findAll_returns_all_todos() {
    adapter.save(Todo.create("Todo 1"));
    adapter.save(Todo.create("Todo 2"));
    List<Todo> todos = adapter.findAll();
    assertThat(todos).hasSize(2);
  }

  @Test
  void deleteById_removes_todo() {
    Todo saved = adapter.save(Todo.create("Buy milk"));
    adapter.deleteById(saved.getId());
    assertThat(adapter.findById(saved.getId())).isEmpty();
  }
}
