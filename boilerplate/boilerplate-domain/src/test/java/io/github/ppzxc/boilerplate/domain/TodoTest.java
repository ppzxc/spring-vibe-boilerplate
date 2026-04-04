package io.github.ppzxc.boilerplate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TodoTest {

  @Test
  void create_returns_new_todo_with_title() {
    Todo todo = Todo.create("Buy milk");

    assertThat(todo.getTitle()).isEqualTo("Buy milk");
    assertThat(todo.isCompleted()).isFalse();
    assertThat(todo.getCreatedAt()).isNotNull();
    assertThat(todo.getUpdatedAt()).isNotNull();
  }

  @Test
  void create_throws_when_title_is_blank() {
    assertThatThrownBy(() -> Todo.create("  ")).isInstanceOf(DomainException.class);
  }

  @Test
  void complete_sets_completed_true() {
    Todo todo = Todo.create("Buy milk");
    Todo completed = todo.complete();
    assertThat(completed.isCompleted()).isTrue();
    assertThat(completed.getUpdatedAt()).isAfterOrEqualTo(todo.getUpdatedAt());
  }

  @Test
  void uncomplete_sets_completed_false() {
    Todo todo = Todo.create("Buy milk").complete();
    Todo uncompleted = todo.uncomplete();
    assertThat(uncompleted.isCompleted()).isFalse();
  }

  @Test
  void updateTitle_changes_title() {
    Todo todo = Todo.create("Buy milk");
    Todo updated = todo.updateTitle("Buy eggs");
    assertThat(updated.getTitle()).isEqualTo("Buy eggs");
    assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(todo.getUpdatedAt());
  }

  @Test
  void updateTitle_throws_when_blank() {
    Todo todo = Todo.create("Buy milk");
    assertThatThrownBy(() -> todo.updateTitle("")).isInstanceOf(DomainException.class);
  }

  @Test
  void reconstitute_restores_all_fields() {
    LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
    LocalDateTime updated = LocalDateTime.of(2026, 1, 2, 0, 0);
    Todo todo = Todo.reconstitute(1L, "Buy milk", true, created, updated);
    assertThat(todo.getId()).isEqualTo(1L);
    assertThat(todo.getTitle()).isEqualTo("Buy milk");
    assertThat(todo.isCompleted()).isTrue();
    assertThat(todo.getCreatedAt()).isEqualTo(created);
    assertThat(todo.getUpdatedAt()).isEqualTo(updated);
  }
}
