package io.github.ppzxc.boilerplate.application.port.input.query;

import io.github.ppzxc.boilerplate.domain.Todo;
import java.util.List;

public interface FindTodoQuery {
  Todo findById(long id);

  List<Todo> findAll();
}
