package io.github.ppzxc.template.application.port.input.query;

import io.github.ppzxc.template.domain.Todo;
import java.util.List;

public interface FindTodoQuery {
  Todo findById(long id);

  List<Todo> findAll();
}
