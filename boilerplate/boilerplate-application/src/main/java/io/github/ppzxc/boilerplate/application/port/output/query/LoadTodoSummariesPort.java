package io.github.ppzxc.boilerplate.application.port.output.query;

import io.github.ppzxc.boilerplate.application.dto.TodoSummary;
import java.util.List;

public interface LoadTodoSummariesPort {
    List<TodoSummary> loadAllSummaries();
}