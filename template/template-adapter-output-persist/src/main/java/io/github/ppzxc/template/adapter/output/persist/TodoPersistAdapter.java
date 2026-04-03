package io.github.ppzxc.template.adapter.output.persist;

import static io.github.ppzxc.template.adapter.output.persist.jooq.Tables.TODO;

import io.github.ppzxc.template.application.port.output.command.DeleteTodoPort;
import io.github.ppzxc.template.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.template.application.port.output.query.FindTodoPort;
import io.github.ppzxc.template.domain.Todo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;

/** jOOQ-based persistence adapter for {@link Todo} aggregate. */
@RequiredArgsConstructor
public class TodoPersistAdapter implements SaveTodoPort, DeleteTodoPort, FindTodoPort {

  private final DSLContext dsl;

  @Override
  public Todo save(Todo todo) {
    if (todo.getId() == null) {
      return insert(todo);
    }
    return update(todo);
  }

  private Todo insert(Todo todo) {
    Record record =
        dsl.insertInto(TODO)
            .set(TODO.TITLE, todo.getTitle())
            .set(TODO.COMPLETED, todo.isCompleted())
            .set(TODO.CREATED_AT, todo.getCreatedAt())
            .set(TODO.UPDATED_AT, todo.getUpdatedAt())
            .returning()
            .fetchOne();
    if (record == null) {
      throw new IllegalStateException("Failed to insert todo");
    }
    return toDomain(record);
  }

  private Todo update(Todo todo) {
    Long id = todo.getId();
    if (id == null) {
      throw new IllegalStateException("Cannot update todo without id");
    }
    dsl.update(TODO)
        .set(TODO.TITLE, todo.getTitle())
        .set(TODO.COMPLETED, todo.isCompleted())
        .set(TODO.UPDATED_AT, todo.getUpdatedAt())
        .where(TODO.ID.eq(id))
        .execute();
    return findById(id)
        .orElseThrow(() -> new IllegalStateException("Failed to update todo: " + id));
  }

  @Override
  public void deleteById(long id) {
    dsl.deleteFrom(TODO).where(TODO.ID.eq(id)).execute();
  }

  @Override
  public Optional<Todo> findById(long id) {
    return dsl.selectFrom(TODO).where(TODO.ID.eq(id)).fetchOptional(this::toDomain);
  }

  @Override
  public List<Todo> findAll() {
    return dsl.selectFrom(TODO).fetch(this::toDomain);
  }

  private Todo toDomain(Record record) {
    return Todo.reconstitute(
        record.get(TODO.ID),
        record.get(TODO.TITLE),
        record.get(TODO.COMPLETED),
        record.get(TODO.CREATED_AT),
        record.get(TODO.UPDATED_AT));
  }
}
