package io.github.ppzxc.boilerplate.application.port.output.query;

import io.github.ppzxc.boilerplate.domain.Todo;
import java.util.List;
import java.util.Optional;

public interface FindTodoPort {
  Optional<Todo> findById(long id);

  List<Todo> findAll();
}
