package io.github.ppzxc.boilerplate.adapter.output.persist;

import static io.github.ppzxc.boilerplate.adapter.output.persist.jooq.Tables.TAG;

import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTagPort;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTagPort;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTagPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import io.github.ppzxc.boilerplate.domain.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

/**
 * jOOQ-based persistence adapter for {@link Tag} aggregate.
 *
 * <p>{@link DataAccessException}을 {@link DomainException}으로 변환하여 헥사고날 경계를 지킨다.
 */
@RequiredArgsConstructor
public class TagPersistAdapter implements SaveTagPort, DeleteTagPort, FindTagPort {

  private final DSLContext dsl;

  @Override
  public Tag save(Tag tag) {
    try {
      Record record = dsl.insertInto(TAG).set(TAG.NAME, tag.getName()).returning().fetchOne();
      if (record == null) {
        throw new DomainException(ErrorCode.INTERNAL, "Failed to insert tag: no record returned");
      }
      return toDomain(record);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during tag save", e);
    }
  }

  @Override
  public void deleteById(long id) {
    try {
      dsl.deleteFrom(TAG).where(TAG.ID.eq(id)).execute();
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during tag delete", e);
    }
  }

  @Override
  public Optional<Tag> findById(long id) {
    try {
      return dsl.selectFrom(TAG).where(TAG.ID.eq(id)).fetchOptional(this::toDomain);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during tag find", e);
    }
  }

  @Override
  public List<Tag> findAll() {
    try {
      return dsl.selectFrom(TAG).fetch(this::toDomain);
    } catch (DataAccessException e) {
      throw new DomainException(ErrorCode.INTERNAL, "Persistence error during tag findAll", e);
    }
  }

  private Tag toDomain(Record record) {
    return Tag.reconstitute(record.get(TAG.ID), record.get(TAG.NAME));
  }
}
