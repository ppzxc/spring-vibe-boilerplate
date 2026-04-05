package io.github.ppzxc.boilerplate.adapter.input.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.ppzxc.boilerplate.domain.Tag;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TagResponse(@Nullable Long id, String name) {

  public static TagResponse from(Tag tag) {
    return new TagResponse(tag.getId(), tag.getName());
  }
}
