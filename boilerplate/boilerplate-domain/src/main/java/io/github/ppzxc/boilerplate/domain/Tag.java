package io.github.ppzxc.boilerplate.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Tag 도메인 엔티티.
 *
 * <p>두 번째 도메인 예제 — Todo와 독립적인 별도 Aggregate. 동일한 헥사고날 아키텍처 패턴을 따른다.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag {

  private @Nullable Long id;
  private String name;

  public static Tag create(String name) {
    if (name == null || name.isBlank()) {
      throw new DomainException(ErrorCode.INVALID_ARGUMENT, "Tag name must not be blank");
    }
    if (name.length() > 50) {
      throw new DomainException(ErrorCode.INVALID_ARGUMENT, "Tag name must not exceed 50 chars");
    }
    return new Tag(null, name.strip());
  }

  public static Tag reconstitute(Long id, String name) {
    return new Tag(id, name);
  }
}
