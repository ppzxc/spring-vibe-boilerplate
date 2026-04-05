package io.github.ppzxc.boilerplate.application.service.query;

import io.github.ppzxc.boilerplate.application.port.input.query.FindTagQuery;
import io.github.ppzxc.boilerplate.application.port.output.query.FindTagPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import io.github.ppzxc.boilerplate.domain.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindTagService implements FindTagQuery {

  private final FindTagPort findTagPort;

  @Override
  public Tag findById(long id) {
    return findTagPort
        .findById(id)
        .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Tag not found: " + id));
  }

  @Override
  public List<Tag> findAll() {
    return findTagPort.findAll();
  }
}
