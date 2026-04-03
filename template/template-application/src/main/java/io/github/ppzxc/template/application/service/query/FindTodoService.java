package io.github.ppzxc.template.application.service.query;

import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.template.application.port.output.query.FindTodoPort;
import io.github.ppzxc.template.domain.DomainException;
import io.github.ppzxc.template.domain.ErrorCode;
import io.github.ppzxc.template.domain.Todo;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindTodoService implements FindTodoQuery {

  private final FindTodoPort findTodoPort;

  @Override
  public Todo findById(long id) {
    return findTodoPort
        .findById(id)
        .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Todo not found: " + id));
  }

  @Override
  public List<Todo> findAll() {
    return findTodoPort.findAll();
  }
}
