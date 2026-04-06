package io.github.ppzxc.boilerplate.adapter.output.persist;

import static io.github.ppzxc.boilerplate.adapter.output.persist.jooq.Tables.TODO;

import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTodoPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTodoPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import io.github.ppzxc.boilerplate.domain.Todo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

/**
 * jOOQ-based persistence adapter for {@link Todo} aggregate.
 *
 * <p>기술 예외({@link DataAccessException})를 도메인 예외({@link DomainException})로 변환하여 헥사고날 경계를 지킨다.
 * application 레이어는 jOOQ에 의존하지 않는다.
 */
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
    try {
      Record record =
          dsl.insertInto(TODO)
              .set(TODO.TITLE, todo.getTitle())
              .set(TODO.COMPLETED, todo.isCompleted())
              .set(TODO.CREATED_AT, todo.getCreatedAt())
              .set(TODO.UPDATED_AT, todo.getUpdatedAt())
              .returning()
              .fetchOne();
      if (record == null) {
        throw new DomainException(ErrorCode.INTERNAL, "Failed to insert todo: no record returned");
      }
      return toDomain(record);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during todo insert", e);
    }
  }

  private Todo update(Todo todo) {
    Long id = todo.getId();
    if (id == null) {
      throw new DomainException(ErrorCode.INTERNAL, "Cannot update todo without id");
    }
    try {
      dsl.update(TODO)
          .set(TODO.TITLE, todo.getTitle())
          .set(TODO.COMPLETED, todo.isCompleted())
          .set(TODO.UPDATED_AT, todo.getUpdatedAt())
          .where(TODO.ID.eq(id))
          .execute();
      return findById(id)
          .orElseThrow(
              () -> new DomainException(ErrorCode.NOT_FOUND, "Todo not found after update: " + id));
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during todo update", e);
    }
  }

  @Override
  public void deleteById(long id) {
    try {
      dsl.deleteFrom(TODO).where(TODO.ID.eq(id)).execute();
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during todo delete", e);
    }
  }

  @Override
  public Optional<Todo> findById(long id) {
    try {
      return dsl.selectFrom(TODO).where(TODO.ID.eq(id)).fetchOptional(this::toDomain);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during todo find", e);
    }
  }

  @Override
  public List<Todo> findAll() {
    try {
      return dsl.selectFrom(TODO).fetch(this::toDomain);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during todo findAll", e);
    }
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
