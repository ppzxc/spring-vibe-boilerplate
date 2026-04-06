package io.github.ppzxc.boilerplate.application.port.input.query;

import io.github.ppzxc.boilerplate.application.dto.TodoSummary;
import java.util.List;

public interface FindTodoSummariesQuery {
    List<TodoSummary> findAllSummaries();
}